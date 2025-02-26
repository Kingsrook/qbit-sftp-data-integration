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
import com.kingsrook.qbits.sftpdataintegration.model.SFTPConnection;
import com.kingsrook.qbits.sftpdataintegration.model.SFTPExportConfig;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReport;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import org.apache.commons.lang3.BooleanUtils;


/*******************************************************************************
 ** prepare for rendering reports for SFTP Exports
 *******************************************************************************/
public class RenderReportForSFTPExportTransformStep extends AbstractTransformStep
{
   private static final QLogger LOG = QLogger.getLogger(RenderReportForSFTPExportTransformStep.class);

   private ProcessSummaryLine okToRenderLine = new ProcessSummaryLine(Status.OK)
      .withMessageSuffix(" generated")
      .withSingularFutureMessage("will be")
      .withPluralFutureMessage("will be")
      .withSingularPastMessage("was")
      .withPluralPastMessage("were");

   private List<ProcessSummaryLine> errorLines             = new ArrayList<>();
   private ProcessSummaryLine       missingConnectionLine  = makeErrorSummaryLine(errorLines, "missing an SFTP Connection");
   private ProcessSummaryLine       inactiveConnectionLine = makeErrorSummaryLine(errorLines, "an inactive SFTP Connection");
   private ProcessSummaryLine       inactiveConfigLine     = makeErrorSummaryLine(errorLines, "being marked inactive");
   private ProcessSummaryLine       missingReportLine      = makeErrorSummaryLine(errorLines, "missing a Report");



   /***************************************************************************
    **
    ***************************************************************************/
   private static ProcessSummaryLine makeErrorSummaryLine(List<ProcessSummaryLine> errorLines, String messageSuffixSuffix)
   {
      ProcessSummaryLine processSummaryLine = new ProcessSummaryLine(Status.ERROR)
         .withMessageSuffix(" generated due to " + messageSuffixSuffix)
         .withSingularFutureMessage("will not be")
         .withPluralFutureMessage("will not be")
         .withSingularPastMessage("was not")
         .withPluralPastMessage("were not");
      errorLines.add(processSummaryLine);
      return (processSummaryLine);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> rs = new ArrayList<>();
      okToRenderLine.addSelfToListIfAnyCount(rs);
      errorLines.forEach(errorLine -> errorLine.addSelfToListIfAnyCount(rs));
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      for(SFTPExportConfig sftpExportConfig : runBackendStepInput.getRecordsAsEntities(SFTPExportConfig.class))
      {
         if(!BooleanUtils.isTrue(sftpExportConfig.getIsActive()))
         {
            inactiveConfigLine.incrementCountAndAddPrimaryKey(sftpExportConfig.getId());
            continue;
         }

         QRecord sftpConnectionRecord = GetAction.execute(SFTPConnection.TABLE_NAME, sftpExportConfig.getSftpConnectionId());
         if(sftpConnectionRecord == null)
         {
            missingConnectionLine.incrementCountAndAddPrimaryKey(sftpExportConfig.getId());
            continue;
         }
         SFTPConnection sftpConnection = new SFTPConnection(sftpConnectionRecord);

         if(!BooleanUtils.isTrue(sftpConnection.getIsActive()))
         {
            inactiveConnectionLine.incrementCountAndAddPrimaryKey(sftpExportConfig.getId());
            continue;
         }

         QRecord savedReportRecord = GetAction.execute(SavedReport.TABLE_NAME, sftpExportConfig.getSavedReportId());
         if(savedReportRecord == null)
         {
            missingReportLine.incrementCountAndAddPrimaryKey(sftpExportConfig.getId());
         }

         okToRenderLine.incrementCountAndAddPrimaryKey(sftpExportConfig.getId());
         runBackendStepOutput.addRecord(sftpExportConfig.toQRecord());
      }
   }

}
