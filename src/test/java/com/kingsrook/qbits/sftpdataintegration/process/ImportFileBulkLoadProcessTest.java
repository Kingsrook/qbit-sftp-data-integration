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
import com.kingsrook.qbits.sftpdataintegration.BaseTest;
import com.kingsrook.qbits.sftpdataintegration.metadata.ImportFileBulkLoadProcessMetaDataProducer;
import com.kingsrook.qbits.sftpdataintegration.metadata.SFTPImportStagingFileTableMetaDataProducer;
import com.kingsrook.qbits.sftpdataintegration.model.ImportFile;
import com.kingsrook.qbits.sftpdataintegration.model.ImportFileStatusEnum;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.savedbulkloadprofiles.SavedBulkLoadProfile;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkInsertMapping;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadProfile;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadProfileField;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for ImportFileBulkLoadProcessStep
 *******************************************************************************/
class ImportFileBulkLoadProcessTest extends BaseTest
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
      Integer savedBulkLoadProfileId = new InsertAction().execute(new InsertInput(SavedBulkLoadProfile.TABLE_NAME).withRecordEntity(new SavedBulkLoadProfile()
         .withLabel("Test Profile")
         .withTableName(ImportFile.TABLE_NAME)
         .withMappingJson(JsonUtils.toJson(new BulkLoadProfile()
            .withHasHeaderRow(true)
            .withVersion("v1")
            .withLayout(BulkInsertMapping.Layout.FLAT.getPossibleValueId())
            .withFieldList(new ArrayList<>(List.of(
               new BulkLoadProfileField().withFieldName("stagedPath").withHeaderName("Staged Path"),
               new BulkLoadProfileField().withFieldName("sourcePath").withDefaultValue("anywhere"),
               new BulkLoadProfileField().withFieldName("importFileStatusId").withHeaderName("state").withDoValueMapping(true)
                  .withValueMappings(Map.of("ok", ImportFileStatusEnum.COMPLETE.getId(), "oops", ImportFileStatusEnum.ERROR.getId())),
               new BulkLoadProfileField().withFieldName("savedBulkLoadProfileId").withHeaderName("profile").withDoValueMapping(true)
                  .withValueMappings(Map.of("one", 1, "also-one", 1))
            )))
         ))
      )).getRecords().get(0).getValueInteger("id");

      QRecord importFile = new InsertAction().execute(new InsertInput(ImportFile.TABLE_NAME).withRecordEntity(new ImportFile()
         .withImportFileStatusId(ImportFileStatusEnum.PENDING.getId())
         .withStagedPath("myFile.csv")
         .withSavedBulkLoadProfileId(savedBulkLoadProfileId)
      )).getRecords().get(0);

      InsertOutput insertOutput = new InsertAction().execute(new InsertInput(SFTPImportStagingFileTableMetaDataProducer.NAME).withRecord(new QRecord()
         .withValue("fileName", "myFile.csv")
         .withValue("contents", """
            Index,Staged Path,ignoreMe,state,profile
            1,/myFile.csv,whatev,ok,also-one
            2,/yourFile.xlsx,do what i want,oops,one
            """)));

      ////////////////////////////
      // run process under test //
      ////////////////////////////
      RunProcessInput input = new RunProcessInput();
      input.setProcessName(ImportFileBulkLoadProcessMetaDataProducer.NAME);
      input.setCallback(QProcessCallbackFactory.forRecord(importFile));
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      RunProcessOutput runProcessOutput = new RunProcessAction().execute(input);
      System.out.println(runProcessOutput);

      ////////////////
      // assertions //
      ////////////////
      assertEquals(3, new CountAction().execute(new CountInput(ImportFile.TABLE_NAME).withFilter(new QQueryFilter())).getCount());
      List<QRecord> records = QueryAction.execute(ImportFile.TABLE_NAME, new QQueryFilter(new QFilterCriteria("sourcePath", QCriteriaOperator.EQUALS, "anywhere")));

      assertEquals("/myFile.csv", records.get(0).getValueString("stagedPath"));
      assertEquals(ImportFileStatusEnum.COMPLETE.getId(), records.get(0).getValueInteger("importFileStatusId"));
      assertEquals(1, records.get(0).getValueInteger("savedBulkLoadProfileId"));

      assertEquals("/yourFile.xlsx", records.get(1).getValueString("stagedPath"));
      assertEquals(ImportFileStatusEnum.ERROR.getId(), records.get(1).getValueInteger("importFileStatusId"));
      assertEquals(1, records.get(1).getValueInteger("savedBulkLoadProfileId"));
   }

}