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
import com.kingsrook.qbits.sftpdataintegration.model.SFTPExportConfig;
import com.kingsrook.qbits.sftpdataintegration.process.RenderReportForSFTPExportLoadStep;
import com.kingsrook.qbits.sftpdataintegration.process.RenderReportForSFTPExportTransformStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitComponentMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReport;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;


/*******************************************************************************
 ** Meta Data Producer for RenderReportForSFTPExportProcess
 *******************************************************************************/
public class RenderReportForSFTPExportProcessMetaDataProducer extends QBitComponentMetaDataProducer<QProcessMetaData, SFTPDataIntegrationQBitConfig>
{
   public static final String NAME = "RenderReportForSFTPExportProcess";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      QProcessMetaData processMetaData = StreamedETLWithFrontendProcess.processMetaDataBuilder()
         .withName(NAME)
         .withLabel("Render Report for SFTP Export")
         .withTableName(SFTPExportConfig.TABLE_NAME)
         .withSourceTable(SFTPExportConfig.TABLE_NAME)
         .withDestinationTable(SFTPExportConfig.TABLE_NAME)
         .withExtractStepClass(ExtractViaQueryStep.class)
         .withTransformStepClass(RenderReportForSFTPExportTransformStep.class)
         .withLoadStepClass(RenderReportForSFTPExportLoadStep.class)
         .withReviewStepRecordFields(List.of(
            new QFieldMetaData("id", QFieldType.INTEGER),
            new QFieldMetaData("name", QFieldType.STRING),
            new QFieldMetaData("savedReportId", QFieldType.INTEGER).withPossibleValueSourceName(SavedReport.TABLE_NAME)
         ))
         .withPreviewMessage(StreamedETLWithFrontendProcess.DEFAULT_PREVIEW_MESSAGE_PREFIX + " processed")
         .withTransactionLevelAutoCommit()
         .getProcessMetaData();

      processMetaData.setProcessTracerCodeReference(getQBitConfig().getProcessTracerCodeReference());

      return (processMetaData);
   }

}
