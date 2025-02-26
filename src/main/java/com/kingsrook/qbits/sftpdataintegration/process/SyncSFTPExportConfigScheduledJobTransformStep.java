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
import java.util.List;
import com.kingsrook.qbits.sftpdataintegration.SFTPDataIntegrationQBitConfig;
import com.kingsrook.qbits.sftpdataintegration.metadata.RenderReportForSFTPExportProcessMetaDataProducer;
import com.kingsrook.qbits.sftpdataintegration.model.SFTPExportConfig;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJob;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJobParameter;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJobType;
import com.kingsrook.qqq.backend.core.processes.implementations.tablesync.AbstractTableSyncTransformStep;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** process to keep ScheduledJob records sync'ed with the SFTPExportConfig table.
 *******************************************************************************/
public class SyncSFTPExportConfigScheduledJobTransformStep extends AbstractTableSyncTransformStep
{
   private static final QLogger LOG = QLogger.getLogger(SyncSFTPExportConfigScheduledJobTransformStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QRecord populateRecordToStore(RunBackendStepInput runBackendStepInput, QRecord destinationRecord, QRecord sourceRecord) throws QException
   {
      SFTPExportConfig sftpExportConfig = new SFTPExportConfig(sourceRecord);
      ScheduledJob     scheduledJob;

      boolean hasCronString = StringUtils.hasContent(sftpExportConfig.getCronExpression());

      if(destinationRecord == null || destinationRecord.getValue("id") == null)
      {
         ////////////////////////////////////////////////////////////////////////////
         // actually - before we insert a record that we'll be marking as          //
         // inactive, just return null (to not insert) if there is no cron string. //
         ////////////////////////////////////////////////////////////////////////////
         if(!hasCronString)
         {
            return (null);
         }

         SFTPDataIntegrationQBitConfig qBitConfig = (SFTPDataIntegrationQBitConfig) runBackendStepInput.getProcess().getSourceQBitConfig();

         ////////////////////////////////////////////////////////////////////////
         // need to do an insert - set lots of key values in the scheduled job //
         ////////////////////////////////////////////////////////////////////////
         scheduledJob = new ScheduledJob();
         scheduledJob.setLabel("SFTPExportFileSync process for SFTPExportConfig " + sftpExportConfig.getId());
         scheduledJob.setDescription("Job to export files (reports) for SFTP Export Config Id " + sftpExportConfig.getId());
         scheduledJob.setSchedulerName(qBitConfig.getSchedulerName());
         scheduledJob.setType(ScheduledJobType.PROCESS.name());
         scheduledJob.setForeignKeyType(getScheduledJobForeignKeyType());
         scheduledJob.setForeignKeyValue(String.valueOf(sftpExportConfig.getId()));
         scheduledJob.setJobParameters(List.of(
            new ScheduledJobParameter().withKey("processName").withValue(getProcessNameScheduledJobParameter()),
            new ScheduledJobParameter().withKey("recordIds").withValue(ValueUtils.getValueAsString(sftpExportConfig.getId()))
         ));
      }
      else
      {
         //////////////////////////////////////////////////////////////////////////////////
         // else doing an update - populate scheduled job entity from destination record //
         //////////////////////////////////////////////////////////////////////////////////
         scheduledJob = new ScheduledJob(destinationRecord);
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////////
      // only set these fields if they are set (else the scheduled job record would be invalid if we cleared //
      // them out - instead, if these are blank, mark the scheduledJob as inactive to make it not run)       //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(hasCronString)
      {
         scheduledJob.setCronExpression(sftpExportConfig.getCronExpression());
         scheduledJob.setCronTimeZoneId(sftpExportConfig.getCronTimeZoneId());
      }

      //////////////////////////////////////////////////////////////////
      // set the scheduled job as inactive if there is no cron string //
      //////////////////////////////////////////////////////////////////
      scheduledJob.setIsActive(hasCronString);

      return scheduledJob.toQRecord();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getScheduledJobForeignKeyType()
   {
      return "sftpExportConfig";
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String getProcessNameScheduledJobParameter()
   {
      return RenderReportForSFTPExportProcessMetaDataProducer.NAME;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected QQueryFilter getExistingRecordQueryFilter(RunBackendStepInput runBackendStepInput, List<Serializable> sourceKeyList)
   {
      return super.getExistingRecordQueryFilter(runBackendStepInput, sourceKeyList)
         .withCriteria(new QFilterCriteria("foreignKeyType", QCriteriaOperator.EQUALS, getScheduledJobForeignKeyType()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected SyncProcessConfig getSyncProcessConfig()
   {
      return new SyncProcessConfig(SFTPExportConfig.TABLE_NAME, "id", ScheduledJob.TABLE_NAME, "foreignKeyValue", true, true);
   }

}
