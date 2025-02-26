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

package com.kingsrook.qbits.sftpdataintegration.model;


import java.io.Serializable;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qbits.sftpdataintegration.metadata.SyncSFTPImportConfigScheduledJobMetaDataProducer;
import com.kingsrook.qbits.sftpdataintegration.process.SyncSFTPImportConfigScheduledJobTransformStep;
import com.kingsrook.qqq.backend.core.actions.customizers.RecordCustomizerUtilityInterface;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizerInterface;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.QueryOrGetInputInterface;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJob;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.quartz.CronScheduleBuilder;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class SFTPImportConfigCustomizer implements TableCustomizerInterface
{

   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<QRecord> postQuery(QueryOrGetInputInterface queryInput, List<QRecord> records) throws QException
   {
      for(QRecord record : records)
      {
         record.setDisplayValue("cronExpression:" + AdornmentType.TooltipValues.TOOLTIP_DYNAMIC, "lolz");
      }

      return records;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<QRecord> preInsertOrUpdate(AbstractActionInput input, List<QRecord> records, boolean isPreview, Optional<List<QRecord>> oldRecordList) throws QException
   {
      Optional<Map<Serializable, QRecord>> oldRecordMap = oldRecordListToMap("id", oldRecordList);

      for(QRecord record : records)
      {
         String cronExpression = record.getValueString("cronExpression");
         if(StringUtils.hasContent(cronExpression))
         {
            try
            {
               CronScheduleBuilder.cronScheduleNonvalidatedExpression(cronExpression);
            }
            catch(ParseException e)
            {
               record.addError(new BadInputStatusMessage("Cron Expression [" + cronExpression + "] is not valid: " + e.getMessage()));
            }

            String cronTimeZoneId = RecordCustomizerUtilityInterface.getValueFromRecordOrOldRecord("cronTimeZoneId", record, record.getValue("id"), oldRecordMap);
            if(!StringUtils.hasContent(cronTimeZoneId))
            {
               record.addError(new BadInputStatusMessage("If a Expression is used, then a corresponding Time Zone must be selected"));
            }
         }
      }

      return (records);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<QRecord> postInsertOrUpdate(AbstractActionInput input, List<QRecord> records, Optional<List<QRecord>> oldRecordList) throws QException
   {
      runSyncProcess(records);
      return (records);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void runSyncProcess(List<QRecord> records)
   {
      List<Serializable> ids = records.stream()
         .filter(r -> CollectionUtils.nullSafeIsEmpty(r.getErrors()))
         .map(r -> r.getValue("id")).toList();

      if(CollectionUtils.nullSafeIsEmpty(ids))
      {
         return;
      }

      try
      {
         RunProcessInput runProcessInput = new RunProcessInput();
         runProcessInput.setProcessName(SyncSFTPImportConfigScheduledJobMetaDataProducer.NAME);
         runProcessInput.setCallback(QProcessCallbackFactory.forPrimaryKeys("id", ids));
         runProcessInput.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
         RunProcessOutput runProcessOutput = new RunProcessAction().execute(runProcessInput);

         Serializable processSummary = runProcessOutput.getValue(StreamedETLWithFrontendProcess.FIELD_PROCESS_SUMMARY);
      }
      catch(Exception e)
      {
         LOG.warn("Error syncing records with schedules to scheduled jobs table", e, logPair("ids", ids));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QRecord> postDelete(DeleteInput deleteInput, List<QRecord> records) throws QException
   {
      List<String> ids = records.stream()
         .filter(r -> CollectionUtils.nullSafeIsEmpty(r.getErrors()))
         .map(r -> r.getValueString("id")).toList();

      if(ids.isEmpty())
      {
         return (records);
      }

      ///////////////////////////////////////////////////
      // delete any corresponding scheduledJob records //
      ///////////////////////////////////////////////////
      try
      {
         DeleteOutput deleteOutput = new DeleteAction().execute(new DeleteInput(ScheduledJob.TABLE_NAME).withQueryFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("foreignKeyType", QCriteriaOperator.EQUALS, SyncSFTPImportConfigScheduledJobTransformStep.getScheduledJobForeignKeyType()))
            .withCriteria(new QFilterCriteria("foreignKeyValue", QCriteriaOperator.IN, ids))
         ));

      }
      catch(Exception e)
      {
         LOG.warn("Error deleting scheduled jobs for records with schedules", e, logPair("ids", ids));
      }

      return (records);
   }

}
