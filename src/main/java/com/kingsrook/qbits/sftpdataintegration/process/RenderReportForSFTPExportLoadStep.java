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
import com.kingsrook.qbits.sftpdataintegration.SFTPDataIntegrationQBitConfig;
import com.kingsrook.qbits.sftpdataintegration.model.SFTPExportConfig;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReport;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractLoadStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ProcessSummaryProviderInterface;
import com.kingsrook.qqq.backend.core.processes.implementations.savedreports.RenderSavedReportExecuteStep;
import com.kingsrook.qqq.backend.core.processes.implementations.savedreports.RenderSavedReportMetaDataProducer;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import org.json.JSONObject;


/*******************************************************************************
 ** render reports for SFTP Export Configs
 *******************************************************************************/
public class RenderReportForSFTPExportLoadStep extends AbstractLoadStep implements ProcessSummaryProviderInterface
{
   private static final QLogger LOG = QLogger.getLogger(RenderReportForSFTPExportLoadStep.class);

   private ProcessSummaryLine okLine = new ProcessSummaryLine(Status.OK)
      .withMessageSuffix(" generated")
      .withSingularFutureMessage("will be")
      .withPluralFutureMessage("will be")
      .withSingularPastMessage("was")
      .withPluralPastMessage("were");

   private ProcessSummaryLine hadErrorLine = new ProcessSummaryLine(Status.ERROR, "had an error generating the report");



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      for(SFTPExportConfig sftpExportConfig : runBackendStepInput.getRecordsAsEntities(SFTPExportConfig.class))
      {
         try
         {
            RunProcessInput input = new RunProcessInput();
            input.setProcessName(RenderSavedReportMetaDataProducer.NAME);
            input.setCallback(QProcessCallbackFactory.forPrimaryKey("id", sftpExportConfig.getSavedReportId()));
            input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);

            //////////////////////////////////////////////////////
            // set up variant for the backend, if so configured //
            //////////////////////////////////////////////////////
            SFTPDataIntegrationQBitConfig config                   = (SFTPDataIntegrationQBitConfig) runBackendStepInput.getProcess().getSourceQBitConfig();
            String                        destinationFileTableName = config.getEffectiveDestinationFileTableName();
            QBackendMetaData             destinationFileBackend   = QContext.getQInstance().getBackendForTable(destinationFileTableName);
            if(destinationFileBackend.getUsesVariants())
            {
               String variantTypeKey = destinationFileBackend.getBackendVariantsConfig().getVariantTypeKey();
               QContext.getQSession().setBackendVariants(MapBuilder.of(variantTypeKey, sftpExportConfig.getId()));
            }

            ReportFormat reportFormat = ReportFormat.fromString(sftpExportConfig.getFormat());
            input.addValue(RenderSavedReportMetaDataProducer.FIELD_NAME_REPORT_FORMAT, sftpExportConfig.getFormat());

            QRecord savedReportRecord = GetAction.execute(SavedReport.TABLE_NAME, sftpExportConfig.getSavedReportId());
            String  fileName          = RenderSavedReportExecuteStep.getDownloadFileBaseName(new RunBackendStepInput(), new SavedReport(savedReportRecord)) + "." + reportFormat.getExtension();

            input.addValue(RenderSavedReportMetaDataProducer.FIELD_NAME_STORAGE_TABLE_NAME, destinationFileTableName);
            input.addValue(RenderSavedReportMetaDataProducer.FIELD_NAME_STORAGE_REFERENCE, fileName);

            /////////////////////////////////////////////////////////////////////////////////////
            // if there are input values, pass them along on report input...                   //
            // this could maybe be better (e.g., some object?), but, this is working initially //
            /////////////////////////////////////////////////////////////////////////////////////
            if(StringUtils.hasContent(sftpExportConfig.getInputValues()))
            {
               JSONObject jsonObject = JsonUtils.toJSONObject(sftpExportConfig.getInputValues());
               for(String name : jsonObject.keySet())
               {
                  input.addValue(name, jsonObject.optString(name));
               }
            }

            RunProcessOutput runProcessOutput = new RunProcessAction().execute(input);

            if(runProcessOutput.getException().isPresent())
            {
               hadErrorLine.incrementCountAndAddPrimaryKey(sftpExportConfig.getId());
            }
            else
            {
               okLine.incrementCountAndAddPrimaryKey(sftpExportConfig.getId());
            }
         }
         catch(Exception e)
         {
            hadErrorLine.incrementCountAndAddPrimaryKey(sftpExportConfig.getId());
         }
      }
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
