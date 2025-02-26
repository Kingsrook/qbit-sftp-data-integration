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


import java.util.List;
import com.kingsrook.qbits.sftpdataintegration.SFTPDataIntegrationQBitConfig;
import com.kingsrook.qbits.sftpdataintegration.model.SFTPImportConfig;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.processes.tracing.ProcessTracerKeyRecordMessage;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import org.apache.commons.lang3.BooleanUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class SFTPImportFileSyncExtractStep extends ExtractViaQueryStep
{
   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   protected QQueryFilter getQueryFilter(RunBackendStepInput runBackendStepInput) throws QException
   {
      Integer sftpImportConfigId = getSftpImportConfigId(runBackendStepInput);
      runBackendStepInput.traceMessage(new ProcessTracerKeyRecordMessage(SFTPImportConfig.TABLE_NAME, sftpImportConfigId));

      QRecord sftpImportConfig = GetAction.execute(SFTPImportConfig.TABLE_NAME, sftpImportConfigId);
      runBackendStepInput.addValue("sftpImportConfig", sftpImportConfig);

      if(!BooleanUtils.isTrue(sftpImportConfig.getValueBoolean("isActive")))
      {
         throw (new QUserFacingException("The selected SFTP Import Config is not active."));
      }

      SFTPDataIntegrationQBitConfig config              = (SFTPDataIntegrationQBitConfig) runBackendStepInput.getProcess().getSourceQBitConfig();
      String                        sourceFileTableName = config.getEffectiveSourceFileTableName();
      QBackendMetaData             sourceFileBackend   = QContext.getQInstance().getBackendForTable(sourceFileTableName);
      if(sourceFileBackend.getUsesVariants())
      {
         String variantTypeKey = sourceFileBackend.getBackendVariantsConfig().getVariantTypeKey();
         QContext.getQSession().setBackendVariants(MapBuilder.of(variantTypeKey, sftpImportConfigId));
      }

      ////////////////////////////////////////////////////////
      // translate settings on the importConfig to a filter //
      ////////////////////////////////////////////////////////
      QQueryFilter filter = new QQueryFilter();

      String fileNamePattern = sftpImportConfig.getValueString("fileNamePattern");
      if(StringUtils.hasContent(fileNamePattern))
      {
         filter.addCriteria(new QFilterCriteria("fileName", QCriteriaOperator.LIKE, fileNamePattern));
      }

      ////////////////////////////////////////////////////////////////////////////////////
      // for the execute step, make sure heavy-fields (e.g., file contents) are fetched //
      ////////////////////////////////////////////////////////////////////////////////////
      if(runBackendStepInput.getStepName().equals(StreamedETLWithFrontendProcess.STEP_NAME_EXECUTE))
      {
         runBackendStepInput.addValue(StreamedETLWithFrontendProcess.FIELD_FETCH_HEAVY_FIELDS, true);
      }

      return (filter);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private Integer getSftpImportConfigId(RunBackendStepInput runBackendStepInput) throws QException
   {
      Integer sftpImportConfigId = runBackendStepInput.getValueInteger("sftpImportConfigId");
      if(sftpImportConfigId == null)
      {
         QQueryFilter filter = super.getQueryFilter(runBackendStepInput);

         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // so - if we didn't take in an sftpImportConfigId as a parameter (e.g., as a scheduled job), then we're assuming //
         // we took in recordIds, as in, a default way a process gets launched.  So - we called super, and that would have //
         // set up a filter - assuming this process's sourceTable - which is a filesystem table, that has fileName as its  //
         // primary key... so, if we see that happening, switch the criteria's field name from fileName to id...           //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         for(QFilterCriteria criteria : CollectionUtils.nonNullList(filter.getCriteria()))
         {
            if(criteria.getFieldName().equals("fileName"))
            {
               criteria.setFieldName("id");
            }
         }

         List<QRecord> sftpImportConfigs = QueryAction.execute(SFTPImportConfig.TABLE_NAME, filter);
         if(sftpImportConfigs.isEmpty())
         {
            throw (new QUserFacingException("No sftpImportConfig found"));
         }
         else if(sftpImportConfigs.size() > 1)
         {
            throw (new QUserFacingException("Only one sftpImportConfig may be selected at a time."));
         }
         else // just 1
         {
            sftpImportConfigId = sftpImportConfigs.get(0).getValueInteger("id");
            runBackendStepInput.addValue("sftpImportConfigId", sftpImportConfigId);
            return sftpImportConfigId;
         }
      }

      return sftpImportConfigId;
   }
}
