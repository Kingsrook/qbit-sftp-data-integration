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


import java.util.List;
import com.kingsrook.qbits.sftpdataintegration.SFTPDataIntegrationQBitConfig;
import com.kingsrook.qbits.sftpdataintegration.model.ImportFile;
import com.kingsrook.qbits.sftpdataintegration.model.SFTPImportConfig;
import com.kingsrook.qbits.sftpdataintegration.process.SFTPImportFileSyncExtractStep;
import com.kingsrook.qbits.sftpdataintegration.process.SFTPImportFileSyncLoadStep;
import com.kingsrook.qbits.sftpdataintegration.process.SFTPImportFileSyncTransformStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.VariantRunStrategy;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitComponentMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;


/*******************************************************************************
 ** Meta Data Producer for SFTPImportFileSync
 **
 ** This process exists on the SFTP Import Config table.
 **
 ** This process gets scheduled to run every-so-often, looking for new files in
 ** the SFTP source table associated with the connection, and then sync'ing them
 ** to the staging filesystem table and the ImportFile database table.
 *******************************************************************************/
public class SFTPImportFileSyncProcessMetaDataProducer extends QBitComponentMetaDataProducer<QProcessMetaData, SFTPDataIntegrationQBitConfig>
{
   public static final String NAME = "SFTPImportFileSyncProcess";



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public int getSortOrder()
   {
      /////////////////////////////////////////////////////////////
      // needs to run after source-file-table, so, make that be. //
      /////////////////////////////////////////////////////////////
      return new SFTPImportSourceFileTableMetaDataProducer().getSortOrder() + 1;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      SFTPDataIntegrationQBitConfig qBitConfig          = getQBitConfig();
      String                        sourceFileTableName = qBitConfig.getEffectiveSourceFileTableName();

      QProcessMetaData processMetaData = StreamedETLWithFrontendProcess.processMetaDataBuilder()
         .withName(NAME)
         .withLabel("SFTP Import File Sync")
         .withIcon(new QIcon().withName("cloud_sync"))
         .withTableName(SFTPImportConfig.TABLE_NAME)
         .withSourceTable(sourceFileTableName)
         .withDestinationTable(ImportFile.TABLE_NAME)
         .withExtractStepClass(SFTPImportFileSyncExtractStep.class)
         .withTransformStepClass(SFTPImportFileSyncTransformStep.class)
         .withLoadStepClass(SFTPImportFileSyncLoadStep.class)
         .withReviewStepRecordFields(List.of(
            new QFieldMetaData("sourcePath", QFieldType.STRING),
            new QFieldMetaData("stagedPath", QFieldType.STRING)
         ))
         .withTransactionLevelAutoCommit()
         .getProcessMetaData();

      //////////////////////////////////////////////////////////////////////////////////
      // if the source-file table uses variants, set that variant data on the process //
      //////////////////////////////////////////////////////////////////////////////////
      QTableMetaData   sourceFileTable   = qInstance.getTable(sourceFileTableName);
      QBackendMetaData sourceFileBackend = qInstance.getBackend(sourceFileTable.getBackendName());
      if(sourceFileBackend.getUsesVariants())
      {
         processMetaData.withVariantBackend(sourceFileBackend.getName());
         processMetaData.withVariantRunStrategy(VariantRunStrategy.SERIAL);
      }

      processMetaData.setProcessTracerCodeReference(getQBitConfig().getProcessTracerCodeReference());

      return (processMetaData);
   }

}
