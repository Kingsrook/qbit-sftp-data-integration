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
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitComponentMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.FilesystemTableMetaDataBuilder;


/*******************************************************************************
 ** Meta Data Producer for SFTPImportFileSourceTable
 *******************************************************************************/
public class SFTPImportSourceFileTableMetaDataProducer extends QBitComponentMetaDataProducer<QTableMetaData, SFTPDataIntegrationQBitConfig>
{
   public static final String NAME = "SFTPImportSourceFileTable";



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public boolean isEnabled()
   {
      return (getQBitConfig().getSourceFileTableConfig().getDoProvideTable());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QTableMetaData produce(QInstance qInstance) throws QException
   {
      QTableMetaData table = new FilesystemTableMetaDataBuilder()
         .withName(NAME)
         .withBackend(qInstance.getBackend(getQBitConfig().getSourceFileTableConfig().getBackendName()))
         .buildStandardCardinalityOneTable()
         .withLabel("SFTP Import Source File")
         .withIcon(new QIcon("drive_file_move"));

      return (table);
   }

}
