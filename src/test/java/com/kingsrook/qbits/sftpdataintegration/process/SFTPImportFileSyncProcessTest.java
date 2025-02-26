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
import java.util.UUID;
import com.kingsrook.qbits.sftpdataintegration.BaseTest;
import com.kingsrook.qbits.sftpdataintegration.metadata.SFTPImportFileSyncProcessMetaDataProducer;
import com.kingsrook.qbits.sftpdataintegration.metadata.SFTPImportSourceFileTableMetaDataProducer;
import com.kingsrook.qbits.sftpdataintegration.metadata.SFTPImportStagingFileTableMetaDataProducer;
import com.kingsrook.qbits.sftpdataintegration.model.ImportFile;
import com.kingsrook.qbits.sftpdataintegration.model.SFTPImportConfig;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for SFTPImportFileSyncProcess
 *******************************************************************************/
class SFTPImportFileSyncProcessTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      //////////////////////
      // set up test data //
      //////////////////////
      QRecord sftpImportConfig = new InsertAction().execute(new InsertInput(SFTPImportConfig.TABLE_NAME).withRecordEntity(new SFTPImportConfig()
         .withName("test")
         .withSftpConnectionId(1)
         .withIsActive(true)
         .withDeleteImportedFiles(false)
         .withFileNamePattern("%.csv")
         .withSavedBulkLoadProfileId(47))).getRecords().get(0);

      String goodFileName = UUID.randomUUID() + ".csv";
      String badFileName  = UUID.randomUUID() + ".txt";
      new InsertAction().execute(new InsertInput(SFTPImportSourceFileTableMetaDataProducer.NAME).withRecords(List.of(
         new QRecord().withValue("fileName", goodFileName).withValue("contents", "one,two,three\n1,2,3\n"),
         new QRecord().withValue("fileName", badFileName).withValue("contents", "Hello, File.")
      )));

      ////////////////////////////
      // run process under test //
      ////////////////////////////
      RunProcessOutput runProcessOutput = runProcess(sftpImportConfig);

      ///////////////////////////////////////////
      // make sure importFile record was built //
      ///////////////////////////////////////////
      List<QRecord> importFileRecords = QueryAction.execute(ImportFile.TABLE_NAME, new QQueryFilter(new QFilterCriteria("sourcePath", QCriteriaOperator.EQUALS, goodFileName)));
      assertEquals(1, importFileRecords.size());

      //////////////////////////////////////////////////
      // make sure file was copied into staging table //
      //////////////////////////////////////////////////
      QRecord stagedFile = GetAction.execute(SFTPImportStagingFileTableMetaDataProducer.NAME, importFileRecords.get(0).getValueString("stagedPath"));
      assertNotNull(stagedFile);

      ////////////////////////////////////////////////////////
      // make sure source file was not deleted (per config) //
      ////////////////////////////////////////////////////////
      QRecord sourceFile = GetAction.execute(SFTPImportSourceFileTableMetaDataProducer.NAME, goodFileName);
      assertNotNull(sourceFile);

      //////////////////////////////////////////
      // make sure bad file was not processed //
      //////////////////////////////////////////
      importFileRecords = QueryAction.execute(ImportFile.TABLE_NAME, new QQueryFilter(new QFilterCriteria("sourcePath", QCriteriaOperator.EQUALS, badFileName)));
      assertEquals(0, importFileRecords.size());
      sourceFile = GetAction.execute(SFTPImportSourceFileTableMetaDataProducer.NAME, badFileName);
      assertNotNull(sourceFile);

      /////////////////////////////////////////////////////
      // switch to delete, re-run, should become deleted //
      /////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(SFTPImportConfig.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", sftpImportConfig.getValue("id"))
         .withValue("deleteImportedFiles", true)));
      runProcess(sftpImportConfig);
      sourceFile = GetAction.execute(SFTPImportSourceFileTableMetaDataProducer.NAME, goodFileName);
      assertNull(sourceFile);

      //////////////////////////////////
      // bad file still sticks around //
      //////////////////////////////////
      sourceFile = GetAction.execute(SFTPImportSourceFileTableMetaDataProducer.NAME, badFileName);
      assertNotNull(sourceFile);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static RunProcessOutput runProcess(QRecord sftpImportConfig) throws QException
   {
      RunProcessInput input = new RunProcessInput();
      input.setProcessName(SFTPImportFileSyncProcessMetaDataProducer.NAME);
      input.setCallback(QProcessCallbackFactory.forRecord(sftpImportConfig));
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      RunProcessOutput runProcessOutput = new RunProcessAction().execute(input);
      return runProcessOutput;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDoesNoopIfInactive() throws QException
   {
      //////////////////////
      // set up test data //
      //////////////////////
      QRecord sftpImportConfig = new InsertAction().execute(new InsertInput(SFTPImportConfig.TABLE_NAME).withRecordEntity(new SFTPImportConfig()
         .withName("test")
         .withSftpConnectionId(1)
         .withIsActive(false)
         .withDeleteImportedFiles(false)
         .withSavedBulkLoadProfileId(47))).getRecords().get(0);

      String goodFileName = UUID.randomUUID() + ".csv";
      new InsertAction().execute(new InsertInput(SFTPImportSourceFileTableMetaDataProducer.NAME).withRecord(
         new QRecord().withValue("fileName", goodFileName).withValue("contents", "one,two,three\n1,2,3\n")));

      ////////////////////////////
      // run process under test //
      ////////////////////////////
      assertThatThrownBy(() -> runProcess(sftpImportConfig)).hasMessageContaining("not active");

      ////////////////////////////////////////////////////////////////
      // make sure importFile record was NOT built (since inactive) //
      ////////////////////////////////////////////////////////////////
      List<QRecord> importFileRecords = QueryAction.execute(ImportFile.TABLE_NAME, new QQueryFilter(new QFilterCriteria("sourcePath", QCriteriaOperator.EQUALS, goodFileName)));
      assertEquals(0, importFileRecords.size());

      /////////////////////////////////////////////////////////////////
      // switch to active; rerun to confirm setup is otherwise valid //
      /////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(SFTPImportConfig.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", sftpImportConfig.getValue("id"))
         .withValue("isActive", true)));
      runProcess(sftpImportConfig);

      importFileRecords = QueryAction.execute(ImportFile.TABLE_NAME, new QQueryFilter(new QFilterCriteria("sourcePath", QCriteriaOperator.EQUALS, goodFileName)));
      assertEquals(1, importFileRecords.size());
   }

}