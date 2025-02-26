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


import java.util.TimeZone;
import com.kingsrook.qbits.sftpdataintegration.BaseTest;
import com.kingsrook.qbits.sftpdataintegration.model.SFTPImportConfig;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJob;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for SyncSFTPImportConfigScheduledTransformStep 
 *******************************************************************************/
class SyncSFTPImportConfigScheduledJobTransformStepTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInsertThenUpdateTwice() throws QException
   {
      assertEquals(0, CountAction.execute(ScheduledJob.TABLE_NAME, new QQueryFilter()));

      ///////////////////////////////////////////////////////////////////////////////////////////////////
      // a little round-about here (let's say "e2e") - but - let's test the scheduled-job sync process //
      // by going through an insert, w/ a cron, that runs a customizer, that runs the process.,        //
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      QRecord sftpImportConfig = new InsertAction().execute(new InsertInput(SFTPImportConfig.TABLE_NAME).withRecordEntity(new SFTPImportConfig()
         .withName("test")
         .withSftpConnectionId(1)
         .withIsActive(true)
         .withDeleteImportedFiles(false)
         .withCronExpression("0 0 0 * * ?")
         .withCronTimeZoneId(TimeZone.getDefault().getID())
         .withSavedBulkLoadProfileId(47))).getRecords().get(0);
      assertEquals(1, CountAction.execute(ScheduledJob.TABLE_NAME, new QQueryFilter()));
      assertEquals("0 0 0 * * ?", GetAction.execute(ScheduledJob.TABLE_NAME, 1).getValueString("cronExpression"));
      assertTrue(GetAction.execute(ScheduledJob.TABLE_NAME, 1).getValueBoolean("isActive"));

      //////////////////////////////////////////////////////////////////////////
      // update schedule in the config - confirm it updates the scheduled job //
      //////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(SFTPImportConfig.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", sftpImportConfig.getValue("id"))
         .withValue("cronExpression", "1 1 1 * * ?")));
      assertEquals(1, CountAction.execute(ScheduledJob.TABLE_NAME, new QQueryFilter()));
      assertEquals("1 1 1 * * ?", GetAction.execute(ScheduledJob.TABLE_NAME, 1).getValueString("cronExpression"));
      assertTrue(GetAction.execute(ScheduledJob.TABLE_NAME, 1).getValueBoolean("isActive"));

      /////////////////////////////////////////////////
      // un-schedule - make sure job is de-activated //
      /////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(SFTPImportConfig.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", sftpImportConfig.getValue("id"))
         .withValue("cronExpression", null)
         .withValue("cronTimeZoneId", null)
      ));
      assertEquals(1, CountAction.execute(ScheduledJob.TABLE_NAME, new QQueryFilter()));
      assertFalse(GetAction.execute(ScheduledJob.TABLE_NAME, 1).getValueBoolean("isActive"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInsertWithoutScheduleDoesNotSchedule() throws QException
   {
      assertEquals(0, CountAction.execute(ScheduledJob.TABLE_NAME, new QQueryFilter()));

      ///////////////////////////////////////////////////////////////////////////////////////////
      // insert a config with no cron fields set - that should make NO scheduled job get built //
      ///////////////////////////////////////////////////////////////////////////////////////////
      QRecord sftpImportConfig = new InsertAction().execute(new InsertInput(SFTPImportConfig.TABLE_NAME).withRecordEntity(new SFTPImportConfig()
         .withName("test")
         .withSftpConnectionId(1)
         .withIsActive(true)
         .withDeleteImportedFiles(false)
         .withCronExpression(null)
         .withCronTimeZoneId(null)
         .withSavedBulkLoadProfileId(47))).getRecords().get(0);
      assertEquals(0, sftpImportConfig.getErrors().size());

      assertEquals(0, CountAction.execute(ScheduledJob.TABLE_NAME, new QQueryFilter()));
   }



   /*******************************************************************************
    ** e.g., the scheduled job should run no matter what - but should just exit
    ** quickly w/ noop if config is inactive (which is tested in SFTPImportFileSyncProcessTest)
    *******************************************************************************/
   @Test
   void testConfigRecordActiveFieldDoesNotImpactScheduledJob() throws QException
   {
      assertEquals(0, CountAction.execute(ScheduledJob.TABLE_NAME, new QQueryFilter()));

      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      // build a config with isActive=false -- the scheduled job should be inserted same as if it was true //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      QRecord sftpImportConfig = new InsertAction().execute(new InsertInput(SFTPImportConfig.TABLE_NAME).withRecordEntity(new SFTPImportConfig()
         .withName("test")
         .withSftpConnectionId(1)
         .withIsActive(false)
         .withDeleteImportedFiles(false)
         .withCronExpression("0 0 0 * * ?")
         .withCronTimeZoneId(TimeZone.getDefault().getID())
         .withSavedBulkLoadProfileId(47))).getRecords().get(0);

      assertEquals(1, CountAction.execute(ScheduledJob.TABLE_NAME, new QQueryFilter()));
      assertEquals("0 0 0 * * ?", GetAction.execute(ScheduledJob.TABLE_NAME, 1).getValueString("cronExpression"));
      assertTrue(GetAction.execute(ScheduledJob.TABLE_NAME, 1).getValueBoolean("isActive"));

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // verify the same thing on update - by first inserting a config w/o a schedule, but then updating it to have a schedule //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      QRecord sftpImportConfig2 = new InsertAction().execute(new InsertInput(SFTPImportConfig.TABLE_NAME).withRecordEntity(new SFTPImportConfig()
         .withName("test2")
         .withSftpConnectionId(1)
         .withIsActive(false)
         .withDeleteImportedFiles(false)
         .withCronExpression(null)
         .withCronTimeZoneId(null)
         .withSavedBulkLoadProfileId(47))).getRecords().get(0);

      assertEquals(1, CountAction.execute(ScheduledJob.TABLE_NAME, new QQueryFilter()));

      new UpdateAction().execute(new UpdateInput(SFTPImportConfig.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", sftpImportConfig2.getValue("id"))
         .withValue("cronExpression", "1 1 1 * * ?")
         .withValue("cronTimeZoneId", TimeZone.getDefault().getID())));
      assertEquals(2, CountAction.execute(ScheduledJob.TABLE_NAME, new QQueryFilter()));
      assertEquals("1 1 1 * * ?", GetAction.execute(ScheduledJob.TABLE_NAME, 2).getValueString("cronExpression"));
      assertTrue(GetAction.execute(ScheduledJob.TABLE_NAME, 2).getValueBoolean("isActive"));
   }

}