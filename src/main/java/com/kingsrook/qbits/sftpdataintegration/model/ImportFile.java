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


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qqq.backend.core.model.savedbulkloadprofiles.SavedBulkLoadProfile;


/*******************************************************************************
 ** QRecord Entity for ImportFile table
 *******************************************************************************/
@QMetaDataProducingEntity(
   produceTableMetaData = true,
   tableMetaDataCustomizer = ImportFile.TableMetaDataCustomizer.class
)
public class ImportFile extends QRecordEntity
{
   public static final String TABLE_NAME = "importFile";



   /***************************************************************************
    **
    ***************************************************************************/
   public static class TableMetaDataCustomizer implements MetaDataCustomizerInterface<QTableMetaData>
   {

      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public QTableMetaData customizeMetaData(QInstance qInstance, QTableMetaData table) throws QException
      {
         table
            .withIcon(new QIcon().withName("upload_file"))
            .withRecordLabelFormat("%s / %s")
            .withRecordLabelFields("sftpImportConfigId", "sourcePath")
            .withSection(SectionFactory.defaultT1("id", "sftpImportConfigId", "sourcePath"))
            .withSection(SectionFactory.defaultT2("importFileStatusId", "savedBulkLoadProfileId", "stagedPath"))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         table.withCustomizer(TableCustomizers.POST_QUERY_RECORD, new QCodeReference(ImportFileCustomizer.class));
         table.getField("stagedPath").withFieldAdornment(new FieldAdornment()
            .withType(AdornmentType.FILE_DOWNLOAD)
            .withValue(AdornmentType.FileDownloadValues.FILE_NAME_FORMAT, "%s")
            .withValue(AdornmentType.FileDownloadValues.FILE_NAME_FORMAT_FIELDS, new ArrayList<>(List.of("stagedPath")))
            .withValue(AdornmentType.FileDownloadValues.DOWNLOAD_URL_DYNAMIC, true));

         ImportFileStatusEnum.addChipAdornmentToField(table.getField("importFileStatusId"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String stagedPath;

   @QField(possibleValueSourceName = SFTPImportConfig.TABLE_NAME, label = "SFTP Import Config")
   private Integer sftpImportConfigId;

   @QField(maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String sourcePath;

   @QField(possibleValueSourceName = SavedBulkLoadProfile.TABLE_NAME, label = "Bulk Load Profile")
   private Integer savedBulkLoadProfileId;

   @QField(possibleValueSourceName = ImportFileStatusEnum.NAME)
   private Integer importFileStatusId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public ImportFile()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public ImportFile(QRecord record)
   {
      populateFromQRecord(record);
   }



   /*******************************************************************************
    ** Getter for id
    *******************************************************************************/
   public Integer getId()
   {
      return (this.id);
   }



   /*******************************************************************************
    ** Setter for id
    *******************************************************************************/
   public void setId(Integer id)
   {
      this.id = id;
   }



   /*******************************************************************************
    ** Fluent setter for id
    *******************************************************************************/
   public ImportFile withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for stagedPath
    *******************************************************************************/
   public String getStagedPath()
   {
      return (this.stagedPath);
   }



   /*******************************************************************************
    ** Setter for stagedPath
    *******************************************************************************/
   public void setStagedPath(String stagedPath)
   {
      this.stagedPath = stagedPath;
   }



   /*******************************************************************************
    ** Fluent setter for stagedPath
    *******************************************************************************/
   public ImportFile withStagedPath(String stagedPath)
   {
      this.stagedPath = stagedPath;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sourcePath
    *******************************************************************************/
   public String getSourcePath()
   {
      return (this.sourcePath);
   }



   /*******************************************************************************
    ** Setter for sourcePath
    *******************************************************************************/
   public void setSourcePath(String sourcePath)
   {
      this.sourcePath = sourcePath;
   }



   /*******************************************************************************
    ** Fluent setter for sourcePath
    *******************************************************************************/
   public ImportFile withSourcePath(String sourcePath)
   {
      this.sourcePath = sourcePath;
      return (this);
   }



   /*******************************************************************************
    ** Getter for createDate
    *******************************************************************************/
   public Instant getCreateDate()
   {
      return (this.createDate);
   }



   /*******************************************************************************
    ** Setter for createDate
    *******************************************************************************/
   public void setCreateDate(Instant createDate)
   {
      this.createDate = createDate;
   }



   /*******************************************************************************
    ** Fluent setter for createDate
    *******************************************************************************/
   public ImportFile withCreateDate(Instant createDate)
   {
      this.createDate = createDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for modifyDate
    *******************************************************************************/
   public Instant getModifyDate()
   {
      return (this.modifyDate);
   }



   /*******************************************************************************
    ** Setter for modifyDate
    *******************************************************************************/
   public void setModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
   }



   /*******************************************************************************
    ** Fluent setter for modifyDate
    *******************************************************************************/
   public ImportFile withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for importFileStatusId
    *******************************************************************************/
   public Integer getImportFileStatusId()
   {
      return (this.importFileStatusId);
   }



   /*******************************************************************************
    ** Setter for importFileStatusId
    *******************************************************************************/
   public void setImportFileStatusId(Integer importFileStatusId)
   {
      this.importFileStatusId = importFileStatusId;
   }



   /*******************************************************************************
    ** Fluent setter for importFileStatusId
    *******************************************************************************/
   public ImportFile withImportFileStatusId(Integer importFileStatusId)
   {
      this.importFileStatusId = importFileStatusId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for savedBulkLoadProfileId
    *******************************************************************************/
   public Integer getSavedBulkLoadProfileId()
   {
      return (this.savedBulkLoadProfileId);
   }



   /*******************************************************************************
    ** Setter for savedBulkLoadProfileId
    *******************************************************************************/
   public void setSavedBulkLoadProfileId(Integer savedBulkLoadProfileId)
   {
      this.savedBulkLoadProfileId = savedBulkLoadProfileId;
   }



   /*******************************************************************************
    ** Fluent setter for savedBulkLoadProfileId
    *******************************************************************************/
   public ImportFile withSavedBulkLoadProfileId(Integer savedBulkLoadProfileId)
   {
      this.savedBulkLoadProfileId = savedBulkLoadProfileId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sftpImportConfigId
    *******************************************************************************/
   public Integer getSftpImportConfigId()
   {
      return (this.sftpImportConfigId);
   }



   /*******************************************************************************
    ** Setter for sftpImportConfigId
    *******************************************************************************/
   public void setSftpImportConfigId(Integer sftpImportConfigId)
   {
      this.sftpImportConfigId = sftpImportConfigId;
   }



   /*******************************************************************************
    ** Fluent setter for sftpImportConfigId
    *******************************************************************************/
   public ImportFile withSftpImportConfigId(Integer sftpImportConfigId)
   {
      this.sftpImportConfigId = sftpImportConfigId;
      return (this);
   }

}
