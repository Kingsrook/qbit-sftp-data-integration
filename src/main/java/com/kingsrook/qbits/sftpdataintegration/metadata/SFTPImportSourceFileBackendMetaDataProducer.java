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


import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import com.kingsrook.qbits.sftpdataintegration.SFTPDataIntegrationQBitConfig;
import com.kingsrook.qbits.sftpdataintegration.model.SFTPConnection;
import com.kingsrook.qbits.sftpdataintegration.model.SFTPImportConfig;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitComponentMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.variants.BackendVariantsConfig;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.lambdas.UnsafeFunction;
import com.kingsrook.qqq.backend.module.filesystem.sftp.actions.AbstractSFTPAction;
import com.kingsrook.qqq.backend.module.filesystem.sftp.model.metadata.SFTPBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.sftp.model.metadata.SFTPBackendVariantSetting;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Meta Data Producer for SFTPImportSourceFile
 *******************************************************************************/
public class SFTPImportSourceFileBackendMetaDataProducer extends QBitComponentMetaDataProducer<QBackendMetaData, SFTPDataIntegrationQBitConfig>
{
   private static final QLogger LOG = QLogger.getLogger(SFTPImportSourceFileBackendMetaDataProducer.class);

   public static final String NAME = "SFTPImportSourceFileBackend";

   public static final String VARIANT_TYPE_KEY = SFTPImportConfig.TABLE_NAME;



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public boolean isEnabled()
   {
      boolean doProvideTable             = getQBitConfig().getSourceFileTableConfig().getDoProvideTable();
      boolean isSourceBackendThisBackend = NAME.equals(getQBitConfig().getSourceFileTableConfig().getBackendName());

      return (doProvideTable && isSourceBackendThisBackend);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QBackendMetaData produce(QInstance qInstance) throws QException
   {
      SFTPBackendMetaData backendMetaData = new SFTPBackendMetaData()
         .withName(NAME);

      backendMetaData.setUsesVariants(true);
      backendMetaData.setBackendVariantsConfig(new BackendVariantsConfig()
         .withOptionsTableName(SFTPImportConfig.TABLE_NAME)
         .withOptionsFilter(new QQueryFilter(new QFilterCriteria("isActive", QCriteriaOperator.EQUALS, true)))
         .withVariantTypeKey(VARIANT_TYPE_KEY)
         .withVariantRecordLookupFunction(new QCodeReference(VariantRecordSupplier.class))
         .withBackendSettingSourceFieldNameMap(Map.of(
            SFTPBackendVariantSetting.USERNAME, SFTPConnection.TABLE_NAME + ".username",
            SFTPBackendVariantSetting.PASSWORD, SFTPConnection.TABLE_NAME + ".password",
            SFTPBackendVariantSetting.HOSTNAME, SFTPConnection.TABLE_NAME + ".hostname",
            SFTPBackendVariantSetting.PORT, SFTPConnection.TABLE_NAME + ".port",
            SFTPBackendVariantSetting.BASE_PATH, "fullPath"
         ))
      );

      /////////////////////////////////////////////////////////////////////
      // if the config has an env-var name with a private-key PEM        //
      // env-var name, then look up that value and add it to the backend //
      /////////////////////////////////////////////////////////////////////
      if(StringUtils.hasContent(getQBitConfig().getSftpPrivateKeyEnvVarName()))
      {
         try
         {
            String pem = new QMetaDataVariableInterpreter().interpret("${env." + getQBitConfig().getSftpPrivateKeyEnvVarName() + "}");
            if(StringUtils.hasContent(pem))
            {
               byte[] privateKeyBytes = AbstractSFTPAction.pemStringToDecodedBytes(pem);
               backendMetaData.setPrivateKey(privateKeyBytes);
            }
         }
         catch(Exception e)
         {
            LOG.warn("Error interpreting private-key PEM env-var into private key bytes.  Private key will NOT be used in this backend", e, logPair("backendName", backendMetaData.getName()), logPair("envVarName", getQBitConfig().getSftpPrivateKeyEnvVarName()));
         }
      }

      return (backendMetaData);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class VariantRecordSupplier implements UnsafeFunction<Serializable, QRecord, QException>
   {
      /***************************************************************************
       **
       ***************************************************************************/
      public QRecord apply(Serializable id) throws QException
      {
         QueryOutput queryOutput = new QueryAction().execute(new QueryInput(SFTPImportConfig.TABLE_NAME)
            .withFilter(new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.EQUALS, id)))
            .withQueryJoin(new QueryJoin(SFTPConnection.TABLE_NAME).withSelect(true)));

         if(queryOutput.getRecords().isEmpty())
         {
            return null;
         }

         QRecord record   = queryOutput.getRecords().get(0);
         String  fullPath = Objects.requireNonNullElse(record.getValueString(SFTPConnection.TABLE_NAME + ".basePath"), "") + "/" + Objects.requireNonNullElse(record.getValueString("subPath"), "");
         record.setValue("fullPath", fullPath);
         return record;
      }
   }

}
