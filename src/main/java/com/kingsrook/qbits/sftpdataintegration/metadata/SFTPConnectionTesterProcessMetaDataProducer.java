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
import com.kingsrook.qbits.sftpdataintegration.model.SFTPConnection;
import com.kingsrook.qbits.sftpdataintegration.process.SFTPConnectionTesterLoadStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.NoopTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;


/*******************************************************************************
 ** Meta Data Producer for SFTPConnectionTester
 *******************************************************************************/
public class SFTPConnectionTesterProcessMetaDataProducer extends MetaDataProducer<QProcessMetaData>
{
   public static final String NAME = "SFTPConnectionTester";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      QProcessMetaData processMetaData = StreamedETLWithFrontendProcess.processMetaDataBuilder()
         .withName(NAME)
         .withLabel("SFTP Connection Tester")
         .withIcon(new QIcon().withName("flaky"))
         .withTableName(SFTPConnection.TABLE_NAME)
         .withSourceTable(SFTPConnection.TABLE_NAME)
         .withDestinationTable(SFTPConnection.TABLE_NAME)
         .withExtractStepClass(ExtractViaQueryStep.class)
         .withTransformStepClass(NoopTransformStep.class)
         .withLoadStepClass(SFTPConnectionTesterLoadStep.class)
         .withReviewStepRecordFields(List.of(
            new QFieldMetaData("id", QFieldType.INTEGER),
            new QFieldMetaData("name", QFieldType.STRING)
         ))
         .withFields(List.of(
            new QFieldMetaData("sftpPrivateKeyEnvVarName", QFieldType.STRING).withDefaultValue("SFTP_PRIVATE_KEY_PEM")
         ))
         .withTransactionLevelAutoCommit()
         .withPreviewMessage("This is a preview of the connections that will be tested")
         .withSupportsFullValidation(false)
         .getProcessMetaData();

      return processMetaData;
   }

}
