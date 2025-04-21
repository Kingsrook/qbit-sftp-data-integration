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

package com.kingsrook.qbits.sftpdataintegration;


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerHelper;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitProducer;


/*******************************************************************************
 **
 *******************************************************************************/
public class SFTPDataIntegrationQBitProducer implements QBitProducer
{
   private static final QLogger LOG = QLogger.getLogger(SFTPDataIntegrationQBitProducer.class);

   private SFTPDataIntegrationQBitConfig sftpDataIntegrationQBitConfig;



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void produce(QInstance qInstance, String namespace) throws QException
   {
      QBitMetaData qBitMetaData = new QBitMetaData()
         .withGroupId("com.kingsrook.qbits")
         .withArtifactId("sftp-data-integration")
         .withVersion("0.1.1")
         .withNamespace(namespace)
         .withConfig(sftpDataIntegrationQBitConfig);
      qInstance.addQBit(qBitMetaData);

      List<MetaDataProducerInterface<?>> producers = MetaDataProducerHelper.findProducers(getClass().getPackageName());
      finishProducing(qInstance, qBitMetaData, sftpDataIntegrationQBitConfig, producers);
   }



   /*******************************************************************************
    ** Getter for sftpDataIntegrationQBitConfig
    *******************************************************************************/
   public SFTPDataIntegrationQBitConfig getSftpDataIntegrationQBitConfig()
   {
      return (this.sftpDataIntegrationQBitConfig);
   }



   /*******************************************************************************
    ** Setter for sftpDataIntegrationQBitConfig
    *******************************************************************************/
   public void setSftpDataIntegrationQBitConfig(SFTPDataIntegrationQBitConfig sftpDataIntegrationQBitConfig)
   {
      this.sftpDataIntegrationQBitConfig = sftpDataIntegrationQBitConfig;
   }



   /*******************************************************************************
    ** Fluent setter for sftpDataIntegrationQBitConfig
    *******************************************************************************/
   public SFTPDataIntegrationQBitProducer withSftpDataIntegrationQBitConfig(SFTPDataIntegrationQBitConfig sftpDataIntegrationQBitConfig)
   {
      this.sftpDataIntegrationQBitConfig = sftpDataIntegrationQBitConfig;
      return (this);
   }


}
