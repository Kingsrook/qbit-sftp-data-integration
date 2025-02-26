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
import com.kingsrook.qbits.sftpdataintegration.model.SFTPImportConfig;
import com.kingsrook.qbits.sftpdataintegration.process.SyncSFTPImportConfigScheduledJobTransformStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.tablesync.TableSyncProcess;


/*******************************************************************************
 ** Meta Data Producer for SyncSFTPImportConfigScheduledJob
 *******************************************************************************/
public class SyncSFTPImportConfigScheduledJobMetaDataProducer extends MetaDataProducer<QProcessMetaData>
{
   public static final String NAME = "syncSFTPImportConfigScheduledJob";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      QProcessMetaData processMetaData = TableSyncProcess.processMetaDataBuilder(false)
         .withName(NAME)
         .withSyncTransformStepClass(SyncSFTPImportConfigScheduledJobTransformStep.class)
         .withReviewStepRecordFields(List.of(
            new QFieldMetaData("sftpImportConfigId", QFieldType.INTEGER).withPossibleValueSourceName(SFTPImportConfig.TABLE_NAME),
            new QFieldMetaData("cronExpression", QFieldType.STRING)
         ))
         .getProcessMetaData()
         .withIsHidden(true);

      return (processMetaData);
   }

}
