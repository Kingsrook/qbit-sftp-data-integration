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


import java.util.List;
import com.kingsrook.qbits.sftpdataintegration.SFTPDataIntegrationQBitConfig;
import com.kingsrook.qbits.sftpdataintegration.metadata.SFTPImportStagingFileTableMetaDataProducer;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizerInterface;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.actions.permissions.TablePermissionSubType;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.QueryOrGetInputInterface;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitConfig;


/*******************************************************************************
 **
 *******************************************************************************/
public class ImportFileCustomizer implements TableCustomizerInterface
{

   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<QRecord> postQuery(QueryOrGetInputInterface queryInput, List<QRecord> records) throws QException
   {
      String     stagingFileTableName = SFTPImportStagingFileTableMetaDataProducer.NAME;
      QBitConfig sourceQBitConfig     = QContext.getQInstance().getTable(ImportFile.TABLE_NAME).getSourceQBitConfig();
      if(sourceQBitConfig instanceof SFTPDataIntegrationQBitConfig sftpDataIntegrationQBitConfig)
      {
         stagingFileTableName = sftpDataIntegrationQBitConfig.getEffectiveStagingFileTableName();
      }

      if(queryInput.getShouldGenerateDisplayValues() && hasStagingFileTableReadPermission(stagingFileTableName))
      {
         for(QRecord record : records)
         {
            String stagedPath = record.getValueString("stagedPath");
            String baseName   = stagedPath.replaceFirst(".*/", "");

            String url = AdornmentType.FileDownloadValues.makeFieldDownloadUrl(stagingFileTableName, stagedPath, "contents", baseName);
            record.setValue("stagedPath:" + AdornmentType.FileDownloadValues.DOWNLOAD_URL_DYNAMIC, url);
         }
      }

      return (records);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static boolean hasStagingFileTableReadPermission(String tableName)
   {
      try
      {
         PermissionsHelper.checkTablePermissionThrowing(new GetInput(tableName), TablePermissionSubType.READ);
         return (true);
      }
      catch(Exception e)
      {
         ///////////////////////////////////////
         // exception indicates no permission //
         ///////////////////////////////////////
      }
      return (false);
   }

}
