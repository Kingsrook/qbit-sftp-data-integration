/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qbits.sftpdataintegration.process;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qbits.sftpdataintegration.SFTPDataIntegrationQBitConfig;
import com.kingsrook.qbits.sftpdataintegration.model.ImportFile;
import com.kingsrook.qbits.sftpdataintegration.model.ImportFileStatusEnum;
import com.kingsrook.qbits.sftpdataintegration.model.SFTPImportConfig;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.general.StandardProcessSummaryLineProducer;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** move files from a source sftp table to our staging table
 *******************************************************************************/
public class SFTPImportFileSyncTransformStep extends AbstractTransformStep
{
   private static final QLogger LOG = QLogger.getLogger(SFTPImportFileSyncTransformStep.class);

   private ProcessSummaryLine okToInsertLine = StandardProcessSummaryLineProducer.getOkToInsertLine()
      .withMessageSuffix(" imported");

   private ProcessSummaryLine alreadyImportedLine = new ProcessSummaryLine(Status.WARNING)
      .withMessageSuffix(" already imported")
      .withSingularFutureMessage("was")
      .withPluralFutureMessage("were")
      .withSingularPastMessage("was")
      .withPluralPastMessage("were");



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> rs = new ArrayList<>();
      okToInsertLine.addSelfToListIfAnyCount(rs);
      alreadyImportedLine.addSelfToListIfAnyCount(rs);
      return (rs);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public Integer getOverrideRecordPipeCapacity(RunBackendStepInput runBackendStepInput)
   {
      return (1);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      QRecord          sftpImportConfigQRecord = (QRecord) runBackendStepInput.getValue("sftpImportConfig");
      SFTPImportConfig sftpImportConfig        = new SFTPImportConfig(sftpImportConfigQRecord);

      if(CollectionUtils.nullSafeIsEmpty(runBackendStepInput.getRecords()))
      {
         return;
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////////
      // send all source paths to the output step - in case we're in delete mode, so we can delete them all. //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////
      ArrayList<String> sourcePaths = new ArrayList<>(runBackendStepInput.getRecords().stream().map(r -> r.getValueString("fileName")).toList());
      runBackendStepOutput.addValue("allSourcePaths", sourcePaths);

      QQueryFilter queryFilter = new QQueryFilter()
         .withCriteria(new QFilterCriteria("sftpImportConfigId", QCriteriaOperator.EQUALS, sftpImportConfig.getId()))
         .withCriteria(new QFilterCriteria("sourcePath", QCriteriaOperator.IN, runBackendStepInput.getRecords().stream().map(r -> r.getValueString("fileName")).toList()));
      List<QRecord> existingRecords     = QueryAction.execute(ImportFile.TABLE_NAME, queryFilter);
      Set<String>   existingSourcePaths = existingRecords.stream().map(r -> r.getValueString("sourcePath")).collect(Collectors.toSet());

      for(QRecord record : runBackendStepInput.getRecords())
      {
         if(existingSourcePaths.contains(record.getValueString("fileName")))
         {
            alreadyImportedLine.incrementCountAndAddPrimaryKey(record.getValueString("fileName"));
         }
         else
         {
            okToInsertLine.incrementCountAndAddPrimaryKey(record.getValueString("fileName"));

            QRecord importFile = new QRecord();
            importFile.setValue("sftpImportConfigId", sftpImportConfig.getId());
            importFile.setValue("savedBulkLoadProfileId", sftpImportConfig.getSavedBulkLoadProfileId());
            importFile.setValue("sourcePath", record.getValueString("fileName"));
            importFile.setValue("importFileStatusId", ImportFileStatusEnum.PENDING.getId());

            //////////////////////////////////////////////////////////////////////
            // copy additional fields over from sftpImportConfig to importFiles //
            // (e.g., application-defined security keys)                        //
            //////////////////////////////////////////////////////////////////////
            SFTPDataIntegrationQBitConfig config = (SFTPDataIntegrationQBitConfig) runBackendStepInput.getProcess().getSourceQBitConfig();
            for(Map.Entry<String, String> entry : CollectionUtils.nonNullMap(config.getAdditionalFieldsToCopyFromSftpImportConfigToImportFile()).entrySet())
            {
               importFile.setValue(entry.getValue(), sftpImportConfigQRecord.getValue(entry.getKey()));
            }

            importFile.setValue("contents", record.getValueByteArray("contents"));

            runBackendStepOutput.addRecord(importFile);
         }
      }
   }

}
