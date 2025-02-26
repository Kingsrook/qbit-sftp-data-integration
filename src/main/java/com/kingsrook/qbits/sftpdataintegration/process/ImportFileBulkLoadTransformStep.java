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
import com.kingsrook.qbits.sftpdataintegration.model.ImportFile;
import com.kingsrook.qbits.sftpdataintegration.model.ImportFileStatusEnum;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.savedbulkloadprofiles.SavedBulkLoadProfile;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;


/*******************************************************************************
 **
 *******************************************************************************/
public class ImportFileBulkLoadTransformStep extends AbstractTransformStep
{
   private static final QLogger LOG = QLogger.getLogger(ImportFileBulkLoadTransformStep.class);

   private ProcessSummaryLine okLine = new ProcessSummaryLine(Status.OK)
      .withMessageSuffix(" bulk loaded")
      .withSingularFutureMessage("will be")
      .withPluralFutureMessage("will be")
      .withSingularPastMessage("was")
      .withPluralPastMessage("were");

   private ProcessSummaryLine alreadyProcessedLine = new ProcessSummaryLine(Status.ERROR)
      .withMessageSuffix(" already been processed")
      .withSingularFutureMessage("has")
      .withPluralFutureMessage("has")
      .withSingularPastMessage("had")
      .withPluralPastMessage("had");

   private ProcessSummaryLine missingProfileLine = new ProcessSummaryLine(Status.ERROR)
      .withMessageSuffix(" missing a bulk load profile")
      .withSingularFutureMessage("is")
      .withPluralFutureMessage("are")
      .withSingularPastMessage("was")
      .withPluralPastMessage("were");

   private ProcessSummaryLine missingBulkLoadProcessLine = new ProcessSummaryLine(Status.ERROR)
      .withMessageSuffix(" referencing a table without a bulk load process")
      .withSingularFutureMessage("is")
      .withPluralFutureMessage("are")
      .withSingularPastMessage("was")
      .withPluralPastMessage("were");



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> rs = new ArrayList<>();
      okLine.addSelfToListIfAnyCount(rs);
      missingProfileLine.addSelfToListIfAnyCount(rs);
      missingBulkLoadProcessLine.addSelfToListIfAnyCount(rs);
      alreadyProcessedLine.addSelfToListIfAnyCount(rs);
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      for(ImportFile importFile : runBackendStepInput.getRecordsAsEntities(ImportFile.class))
      {
         if(ImportFileStatusEnum.COMPLETE.getId().equals(importFile.getImportFileStatusId()))
         {
            alreadyProcessedLine.incrementCountAndAddPrimaryKey(importFile.getId());
            continue;
         }

         QRecord savedBulkLoadProfileRecord = GetAction.execute(SavedBulkLoadProfile.TABLE_NAME, importFile.getSavedBulkLoadProfileId());
         if(savedBulkLoadProfileRecord == null)
         {
            missingProfileLine.incrementCountAndAddPrimaryKey(importFile.getId());
            continue;
         }

         String           tableName           = savedBulkLoadProfileRecord.getValueString("tableName");
         String           bulkLoadProcessName = tableName + ".bulkInsert";
         QProcessMetaData bulkLoadProcess     = QContext.getQInstance().getProcess(bulkLoadProcessName);
         if(bulkLoadProcess == null)
         {
            missingBulkLoadProcessLine.incrementCountAndAddPrimaryKey(importFile.getId());
            continue;
         }

         okLine.incrementCountAndAddPrimaryKey(importFile.getId());
         runBackendStepOutput.addRecord(importFile.toQRecord());
      }
   }

}
