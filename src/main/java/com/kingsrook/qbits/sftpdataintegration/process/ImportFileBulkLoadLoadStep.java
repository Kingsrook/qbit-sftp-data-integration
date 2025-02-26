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


import java.io.Serializable;
import java.util.ArrayList;
import com.kingsrook.qbits.sftpdataintegration.metadata.ImportFileBulkLoadProcessMetaDataProducer;
import com.kingsrook.qbits.sftpdataintegration.model.ImportFile;
import com.kingsrook.qbits.sftpdataintegration.model.ImportFileStatusEnum;
import com.kingsrook.qqq.backend.core.actions.audits.DMLAuditAction;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.storage.StorageInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.savedbulkloadprofiles.SavedBulkLoadProfile;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.BulkInsertStepUtils;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractLoadStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ProcessSummaryProviderInterface;
import com.kingsrook.qqq.backend.core.processes.tracing.ProcessTracerKeyRecordMessage;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class ImportFileBulkLoadLoadStep extends AbstractLoadStep implements ProcessSummaryProviderInterface
{
   private static final QLogger LOG = QLogger.getLogger(ImportFileBulkLoadLoadStep.class);

   private ProcessSummaryLine okLine = new ProcessSummaryLine(Status.OK)
      .withMessageSuffix(" bulk loaded")
      .withSingularFutureMessage("will be")
      .withPluralFutureMessage("will be")
      .withSingularPastMessage("was")
      .withPluralPastMessage("were");

   private ProcessSummaryLine hadErrorLine = new ProcessSummaryLine(Status.ERROR, "had an error running bulk load");



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      for(ImportFile importFile : runBackendStepInput.getRecordsAsEntities(ImportFile.class))
      {
         try
         {
            updateImportFileStatus(importFile.getId(), ImportFileStatusEnum.PROCESSING);

            QRecord savedBulkLoadProfileRecord = GetAction.execute(SavedBulkLoadProfile.TABLE_NAME, importFile.getSavedBulkLoadProfileId());

            String tableName           = savedBulkLoadProfileRecord.getValueString("tableName");
            String bulkLoadProcessName = tableName + ".bulkInsert";

            RunProcessInput runBulkLoadInput = new RunProcessInput();
            runBulkLoadInput.setProcessName(bulkLoadProcessName);
            runBulkLoadInput.addValue("tableName", tableName);
            runBulkLoadInput.addValue("savedBulkLoadProfileId", savedBulkLoadProfileRecord.getValueInteger("id"));
            BulkInsertStepUtils.setHeadless(runBulkLoadInput);

            runBackendStepInput.addValue(DMLAuditAction.AUDIT_CONTEXT_FIELD_NAME, "From Import File " + importFile.getId());

            StorageInput storageInput = new StorageInput(runBackendStepInput.getValueString(ImportFileBulkLoadProcessMetaDataProducer.FIELD_STAGING_TABLE))
               .withReference(importFile.getStagedPath());

            BulkInsertStepUtils.setStorageInputForTheFile(runBulkLoadInput, storageInput);
            BulkInsertStepUtils.setProcessTracerKeyRecordMessage(runBulkLoadInput, new ProcessTracerKeyRecordMessage(ImportFile.TABLE_NAME, importFile.getId()));

            Serializable processTracerCodeReference = runBackendStepInput.getValue(ImportFileBulkLoadProcessMetaDataProducer.FIELD_BULK_LOAD_PROCESS_TRACER_CODE_REFERENCE);
            if(processTracerCodeReference != null)
            {
               runBulkLoadInput.addValue(RunProcessAction.PROCESS_TRACER_CODE_REFERENCE_FIELD, processTracerCodeReference);
            }

            runBulkLoadInput.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
            RunProcessOutput runProcessOutput = new RunProcessAction().execute(runBulkLoadInput);

            /////////////////////////////////////////////////////////////////////////
            // todo we should do something with some output from bulk load, right? //
            /////////////////////////////////////////////////////////////////////////

            okLine.incrementCountAndAddPrimaryKey(importFile.getId());
            updateImportFileStatus(importFile.getId(), ImportFileStatusEnum.COMPLETE);
         }
         catch(Exception e)
         {
            LOG.warn("Error processing import file", e, logPair("id", importFile.getId()));
            hadErrorLine.incrementCountAndAddPrimaryKey(importFile.getId());
            updateImportFileStatus(importFile.getId(), ImportFileStatusEnum.ERROR);
         }
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static void updateImportFileStatus(Integer id, ImportFileStatusEnum status) throws QException
   {
      new UpdateAction().execute(new UpdateInput(ImportFile.TABLE_NAME)
         .withRecord(new ImportFile()
            .withId(id)
            .withImportFileStatusId(status.getId())
            .toQRecordOnlyChangedFields(true)));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> processSummary = getTransformStep().getProcessSummary(runBackendStepOutput, isForResultScreen);
      if(hadErrorLine.getCount() > 0)
      {
         processSummary.removeIf(line -> line.getStatus() == Status.OK);

         okLine.addSelfToListIfAnyCount(processSummary);
         processSummary.add(hadErrorLine);
      }

      return (processSummary);
   }
}
