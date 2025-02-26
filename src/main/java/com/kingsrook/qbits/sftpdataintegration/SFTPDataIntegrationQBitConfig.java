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
import java.util.Map;
import com.kingsrook.qbits.sftpdataintegration.metadata.SFTPExportDestinationFileTableMetaDataProducer;
import com.kingsrook.qbits.sftpdataintegration.metadata.SFTPImportSourceFileTableMetaDataProducer;
import com.kingsrook.qbits.sftpdataintegration.metadata.SFTPImportStagingFileTableMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.ProvidedOrSuppliedTableConfig;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitConfig;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** ad-hoc Configuration data for this qbit.
 **
 *******************************************************************************/
public class SFTPDataIntegrationQBitConfig implements QBitConfig
{
   private MetaDataCustomizerInterface<QTableMetaData> tableMetaDataCustomizer;

   private ProvidedOrSuppliedTableConfig sourceFileTableConfig;
   private ProvidedOrSuppliedTableConfig destinationFileTableConfig;
   private ProvidedOrSuppliedTableConfig stagingFileTableConfig;

   private String sftpPrivateKeyEnvVarName;

   private Map<String, String> additionalFieldsToCopyFromSftpImportConfigToImportFile;

   private QCodeReference processTracerCodeReference;

   private String schedulerName;



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void validate(QInstance qInstance, List<String> errors)
   {
      assertCondition(sourceFileTableConfig != null, "sourceFileTableConfig must not be null", errors);
      assertCondition(destinationFileTableConfig != null, "destinationFileTableConfig must not be null", errors);
      assertCondition(stagingFileTableConfig != null, "stagingFileTableConfig must not be null", errors);
      assertCondition(StringUtils.hasContent(schedulerName), "schedulerName must not be null", errors);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public String getEffectiveSourceFileTableName()
   {
      if(sourceFileTableConfig.getDoProvideTable())
      {
         return (SFTPImportSourceFileTableMetaDataProducer.NAME);
      }

      return (sourceFileTableConfig.getTableName());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public String getEffectiveDestinationFileTableName()
   {
      if(destinationFileTableConfig.getDoProvideTable())
      {
         return (SFTPExportDestinationFileTableMetaDataProducer.NAME);
      }

      return (destinationFileTableConfig.getTableName());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public String getEffectiveStagingFileTableName()
   {
      if(stagingFileTableConfig.getDoProvideTable())
      {
         return (SFTPImportStagingFileTableMetaDataProducer.NAME);
      }

      return (stagingFileTableConfig.getTableName());
   }



   /*******************************************************************************
    ** Getter for processTracerCodeReference
    *******************************************************************************/
   public QCodeReference getProcessTracerCodeReference()
   {
      return (this.processTracerCodeReference);
   }



   /*******************************************************************************
    ** Setter for processTracerCodeReference
    *******************************************************************************/
   public void setProcessTracerCodeReference(QCodeReference processTracerCodeReference)
   {
      this.processTracerCodeReference = processTracerCodeReference;
   }



   /*******************************************************************************
    ** Fluent setter for processTracerCodeReference
    *******************************************************************************/
   public SFTPDataIntegrationQBitConfig withProcessTracerCodeReference(QCodeReference processTracerCodeReference)
   {
      this.processTracerCodeReference = processTracerCodeReference;
      return (this);
   }



   /*******************************************************************************
    ** Getter for schedulerName
    *******************************************************************************/
   public String getSchedulerName()
   {
      return (this.schedulerName);
   }



   /*******************************************************************************
    ** Setter for schedulerName
    *******************************************************************************/
   public void setSchedulerName(String schedulerName)
   {
      this.schedulerName = schedulerName;
   }



   /*******************************************************************************
    ** Fluent setter for schedulerName
    *******************************************************************************/
   public SFTPDataIntegrationQBitConfig withSchedulerName(String schedulerName)
   {
      this.schedulerName = schedulerName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sourceFileTableConfig
    *******************************************************************************/
   public ProvidedOrSuppliedTableConfig getSourceFileTableConfig()
   {
      return (this.sourceFileTableConfig);
   }



   /*******************************************************************************
    ** Setter for sourceFileTableConfig
    *******************************************************************************/
   public void setSourceFileTableConfig(ProvidedOrSuppliedTableConfig sourceFileTableConfig)
   {
      this.sourceFileTableConfig = sourceFileTableConfig;
   }



   /*******************************************************************************
    ** Fluent setter for sourceFileTableConfig
    *******************************************************************************/
   public SFTPDataIntegrationQBitConfig withSourceFileTableConfig(ProvidedOrSuppliedTableConfig sourceFileTableConfig)
   {
      this.sourceFileTableConfig = sourceFileTableConfig;
      return (this);
   }



   /*******************************************************************************
    ** Getter for stagingFileTableConfig
    *******************************************************************************/
   public ProvidedOrSuppliedTableConfig getStagingFileTableConfig()
   {
      return (this.stagingFileTableConfig);
   }



   /*******************************************************************************
    ** Setter for stagingFileTableConfig
    *******************************************************************************/
   public void setStagingFileTableConfig(ProvidedOrSuppliedTableConfig stagingFileTableConfig)
   {
      this.stagingFileTableConfig = stagingFileTableConfig;
   }



   /*******************************************************************************
    ** Fluent setter for stagingFileTableConfig
    *******************************************************************************/
   public SFTPDataIntegrationQBitConfig withStagingFileTableConfig(ProvidedOrSuppliedTableConfig stagingFileTableConfig)
   {
      this.stagingFileTableConfig = stagingFileTableConfig;
      return (this);
   }



   /*******************************************************************************
    ** Getter for additionalFieldsToCopyFromSftpImportConfigToImportFile
    *******************************************************************************/
   public Map<String, String> getAdditionalFieldsToCopyFromSftpImportConfigToImportFile()
   {
      return (this.additionalFieldsToCopyFromSftpImportConfigToImportFile);
   }



   /*******************************************************************************
    ** Setter for additionalFieldsToCopyFromSftpImportConfigToImportFile
    *******************************************************************************/
   public void setAdditionalFieldsToCopyFromSftpImportConfigToImportFile(Map<String, String> additionalFieldsToCopyFromSftpImportConfigToImportFile)
   {
      this.additionalFieldsToCopyFromSftpImportConfigToImportFile = additionalFieldsToCopyFromSftpImportConfigToImportFile;
   }



   /*******************************************************************************
    ** Fluent setter for additionalFieldsToCopyFromSftpImportConfigToImportFile
    *******************************************************************************/
   public SFTPDataIntegrationQBitConfig withAdditionalFieldsToCopyFromSftpImportConfigToImportFile(Map<String, String> additionalFieldsToCopyFromSftpImportConfigToImportFile)
   {
      this.additionalFieldsToCopyFromSftpImportConfigToImportFile = additionalFieldsToCopyFromSftpImportConfigToImportFile;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableMetaDataCustomizer
    *******************************************************************************/
   public MetaDataCustomizerInterface<QTableMetaData> getTableMetaDataCustomizer()
   {
      return (this.tableMetaDataCustomizer);
   }



   /*******************************************************************************
    ** Setter for tableMetaDataCustomizer
    *******************************************************************************/
   public void setTableMetaDataCustomizer(MetaDataCustomizerInterface<QTableMetaData> tableMetaDataCustomizer)
   {
      this.tableMetaDataCustomizer = tableMetaDataCustomizer;
   }



   /*******************************************************************************
    ** Fluent setter for tableMetaDataCustomizer
    *******************************************************************************/
   public SFTPDataIntegrationQBitConfig withTableMetaDataCustomizer(MetaDataCustomizerInterface<QTableMetaData> tableMetaDataCustomizer)
   {
      this.tableMetaDataCustomizer = tableMetaDataCustomizer;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sftpPrivateKeyEnvVarName
    *******************************************************************************/
   public String getSftpPrivateKeyEnvVarName()
   {
      return (this.sftpPrivateKeyEnvVarName);
   }



   /*******************************************************************************
    ** Setter for sftpPrivateKeyEnvVarName
    *******************************************************************************/
   public void setSftpPrivateKeyEnvVarName(String sftpPrivateKeyEnvVarName)
   {
      this.sftpPrivateKeyEnvVarName = sftpPrivateKeyEnvVarName;
   }



   /*******************************************************************************
    ** Fluent setter for sftpPrivateKeyEnvVarName
    *******************************************************************************/
   public SFTPDataIntegrationQBitConfig withSftpPrivateKeyEnvVarName(String sftpPrivateKeyEnvVarName)
   {
      this.sftpPrivateKeyEnvVarName = sftpPrivateKeyEnvVarName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for destinationFileTableConfig
    *******************************************************************************/
   public ProvidedOrSuppliedTableConfig getDestinationFileTableConfig()
   {
      return (this.destinationFileTableConfig);
   }



   /*******************************************************************************
    ** Setter for destinationFileTableConfig
    *******************************************************************************/
   public void setDestinationFileTableConfig(ProvidedOrSuppliedTableConfig destinationFileTableConfig)
   {
      this.destinationFileTableConfig = destinationFileTableConfig;
   }



   /*******************************************************************************
    ** Fluent setter for destinationFileTableConfig
    *******************************************************************************/
   public SFTPDataIntegrationQBitConfig withDestinationFileTableConfig(ProvidedOrSuppliedTableConfig destinationFileTableConfig)
   {
      this.destinationFileTableConfig = destinationFileTableConfig;
      return (this);
   }


}
