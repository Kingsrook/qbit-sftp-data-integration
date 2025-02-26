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

package com.kingsrook.qbits.sftpdataintegration.metadata;


import com.kingsrook.qbits.sftpdataintegration.SFTPDataIntegrationQBitConfig;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitComponentMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.FilesystemTableMetaDataBuilder;


/*******************************************************************************
 ** Meta Data Producer for SFTPImportFileStagingTable
 *******************************************************************************/
public class SFTPImportStagingFileTableMetaDataProducer extends QBitComponentMetaDataProducer<QTableMetaData, SFTPDataIntegrationQBitConfig>
{
   public static final String NAME = "SFTPImportStagingFileTable";



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public boolean isEnabled()
   {
      return (getQBitConfig().getStagingFileTableConfig().getDoProvideTable());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QTableMetaData produce(QInstance qInstance) throws QException
   {
      SFTPDataIntegrationQBitConfig qBitConfig = getQBitConfig();

      QTableMetaData table = new FilesystemTableMetaDataBuilder()
         .withName(NAME)
         .withBackend(qInstance.getBackend(qBitConfig.getStagingFileTableConfig().getBackendName()))
         .buildStandardCardinalityOneTable()
         .withLabel("SFTP Import Staging File")
         .withIcon(new QIcon("snippet_folder"));

      table.getField("contents")
         .withFieldAdornment(new FieldAdornment(AdornmentType.FILE_DOWNLOAD)
            .withValue(AdornmentType.FileDownloadValues.FILE_NAME_FIELD, "fileName"));

      return (table);
   }

}
