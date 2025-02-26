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
import java.util.UUID;
import com.kingsrook.qbits.sftpdataintegration.SFTPDataIntegrationQBitConfig;
import com.kingsrook.qbits.sftpdataintegration.model.SFTPImportConfig;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaInsertStep;
import org.apache.commons.lang3.BooleanUtils;


/*******************************************************************************
 ** store files in both the staging filesystem, and build an import record for them
 *******************************************************************************/
public class SFTPImportFileSyncLoadStep extends LoadViaInsertStep
{
   private static final QLogger LOG = QLogger.getLogger(SFTPImportFileSyncLoadStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      SFTPImportConfig              sftpImportConfig = new SFTPImportConfig((QRecord) runBackendStepInput.getValue("sftpImportConfig"));
      SFTPDataIntegrationQBitConfig config           = (SFTPDataIntegrationQBitConfig) runBackendStepInput.getProcess().getSourceQBitConfig();

      ////////////////////////////////////////////
      // upload the files to the staging tables //
      ////////////////////////////////////////////
      for(QRecord record : runBackendStepInput.getRecords())
      {
         String stagedPath = UUID.randomUUID() + "/" + record.getValueString("sourcePath");
         record.setValue("stagedPath", stagedPath);

         new InsertAction().execute(new InsertInput(config.getEffectiveStagingFileTableName()).withRecord(new QRecord()
            .withValue("fileName", stagedPath)
            .withValue("contents", record.getValueByteArray("contents"))
         ));
      }

      ////////////////////////////////
      // insert them into our table //
      ////////////////////////////////
      super.runOnePage(runBackendStepInput, runBackendStepOutput);

      //////////////////////////////////////////////////////////////////
      // delete all processed files from the source, if so configured //
      //////////////////////////////////////////////////////////////////
      if(BooleanUtils.isTrue(sftpImportConfig.getDeleteImportedFiles()))
      {
         ArrayList<Serializable> sourcePaths = (ArrayList<Serializable>) runBackendStepOutput.getValue("allSourcePaths");
         new DeleteAction().execute(new DeleteInput(config.getEffectiveSourceFileTableName()).withPrimaryKeys(sourcePaths));
      }
   }

}
