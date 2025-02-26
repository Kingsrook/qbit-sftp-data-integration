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
import com.kingsrook.qbits.sftpdataintegration.metadata.SFTPExportConfigReportValuesWidgetMetaDataProducer;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormatPossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.common.TimeZonePossibleValueSourceMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReport;


/*******************************************************************************
 ** QRecord Entity for SFTPExportConfig table
 *******************************************************************************/
@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = SFTPExportConfig.TableMetaDataCustomizer.class
)
public class SFTPExportConfig extends QRecordEntity
{
   public static final String TABLE_NAME = "sftpExportConfig";



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
            .withLabel("SFTP Export Config")
            .withIcon(new QIcon().withName("electrical_services"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("name")
            .withUniqueKey(new UniqueKey("name"))
            .withSection(SectionFactory.defaultT1("id", "name"))
            .withSection(SectionFactory.customT2("settings", new QIcon("settings"), "isActive", "sftpConnectionId", "savedReportId", "format", "subPath"))
            .withSection(SectionFactory.customT2("schedule", new QIcon("schedule"), "cronExpression", "cronTimeZoneId"))
            .withSection(SectionFactory.customT2("variableValues", new QIcon().withName("data_object")).withWidgetName(SFTPExportConfigReportValuesWidgetMetaDataProducer.NAME))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"))
            .withSection(SectionFactory.customT2("hidden", null, "inputValues").withIsHidden(true));

         table.withCustomizer(TableCustomizers.PRE_INSERT_RECORD, new QCodeReference(SFTPExportConfigCustomizer.class));
         table.withCustomizer(TableCustomizers.PRE_UPDATE_RECORD, new QCodeReference(SFTPExportConfigCustomizer.class));
         table.withCustomizer(TableCustomizers.POST_INSERT_RECORD, new QCodeReference(SFTPExportConfigCustomizer.class));
         table.withCustomizer(TableCustomizers.POST_UPDATE_RECORD, new QCodeReference(SFTPExportConfigCustomizer.class));
         table.withCustomizer(TableCustomizers.POST_DELETE_RECORD, new QCodeReference(SFTPExportConfigCustomizer.class));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR, isRequired = true)
   private String name;

   @QField(possibleValueSourceName = SFTPConnection.TABLE_NAME, isRequired = true)
   private Integer sftpConnectionId;

   @QField(possibleValueSourceName = SavedReport.TABLE_NAME, label = "Report", isRequired = true)
   private Integer savedReportId;

   @QField(isRequired = true, defaultValue = "true")
   private Boolean isActive;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String subPath;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String cronExpression;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR, possibleValueSourceName = TimeZonePossibleValueSourceMetaDataProvider.NAME)
   private String cronTimeZoneId;

   @QField(isRequired = true, maxLength = 20, valueTooLongBehavior = ValueTooLongBehavior.ERROR, possibleValueSourceName = ReportFormatPossibleValueEnum.NAME)
   private String format;

   @QField()
   private String inputValues;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public SFTPExportConfig()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public SFTPExportConfig(QRecord record)
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
   public SFTPExportConfig withId(Integer id)
   {
      this.id = id;
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
   public SFTPExportConfig withCreateDate(Instant createDate)
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
   public SFTPExportConfig withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for name
    *******************************************************************************/
   public String getName()
   {
      return (this.name);
   }



   /*******************************************************************************
    ** Setter for name
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   public SFTPExportConfig withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sftpConnectionId
    *******************************************************************************/
   public Integer getSftpConnectionId()
   {
      return (this.sftpConnectionId);
   }



   /*******************************************************************************
    ** Setter for sftpConnectionId
    *******************************************************************************/
   public void setSftpConnectionId(Integer sftpConnectionId)
   {
      this.sftpConnectionId = sftpConnectionId;
   }



   /*******************************************************************************
    ** Fluent setter for sftpConnectionId
    *******************************************************************************/
   public SFTPExportConfig withSftpConnectionId(Integer sftpConnectionId)
   {
      this.sftpConnectionId = sftpConnectionId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for savedReportId
    *******************************************************************************/
   public Integer getSavedReportId()
   {
      return (this.savedReportId);
   }



   /*******************************************************************************
    ** Setter for savedReportId
    *******************************************************************************/
   public void setSavedReportId(Integer savedReportId)
   {
      this.savedReportId = savedReportId;
   }



   /*******************************************************************************
    ** Fluent setter for savedReportId
    *******************************************************************************/
   public SFTPExportConfig withSavedReportId(Integer savedReportId)
   {
      this.savedReportId = savedReportId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isActive
    *******************************************************************************/
   public Boolean getIsActive()
   {
      return (this.isActive);
   }



   /*******************************************************************************
    ** Setter for isActive
    *******************************************************************************/
   public void setIsActive(Boolean isActive)
   {
      this.isActive = isActive;
   }



   /*******************************************************************************
    ** Fluent setter for isActive
    *******************************************************************************/
   public SFTPExportConfig withIsActive(Boolean isActive)
   {
      this.isActive = isActive;
      return (this);
   }



   /*******************************************************************************
    ** Getter for subPath
    *******************************************************************************/
   public String getSubPath()
   {
      return (this.subPath);
   }



   /*******************************************************************************
    ** Setter for subPath
    *******************************************************************************/
   public void setSubPath(String subPath)
   {
      this.subPath = subPath;
   }



   /*******************************************************************************
    ** Fluent setter for subPath
    *******************************************************************************/
   public SFTPExportConfig withSubPath(String subPath)
   {
      this.subPath = subPath;
      return (this);
   }



   /*******************************************************************************
    ** Getter for cronExpression
    *******************************************************************************/
   public String getCronExpression()
   {
      return (this.cronExpression);
   }



   /*******************************************************************************
    ** Setter for cronExpression
    *******************************************************************************/
   public void setCronExpression(String cronExpression)
   {
      this.cronExpression = cronExpression;
   }



   /*******************************************************************************
    ** Fluent setter for cronExpression
    *******************************************************************************/
   public SFTPExportConfig withCronExpression(String cronExpression)
   {
      this.cronExpression = cronExpression;
      return (this);
   }



   /*******************************************************************************
    ** Getter for cronTimeZoneId
    *******************************************************************************/
   public String getCronTimeZoneId()
   {
      return (this.cronTimeZoneId);
   }



   /*******************************************************************************
    ** Setter for cronTimeZoneId
    *******************************************************************************/
   public void setCronTimeZoneId(String cronTimeZoneId)
   {
      this.cronTimeZoneId = cronTimeZoneId;
   }



   /*******************************************************************************
    ** Fluent setter for cronTimeZoneId
    *******************************************************************************/
   public SFTPExportConfig withCronTimeZoneId(String cronTimeZoneId)
   {
      this.cronTimeZoneId = cronTimeZoneId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for format
    *******************************************************************************/
   public String getFormat()
   {
      return (this.format);
   }



   /*******************************************************************************
    ** Setter for format
    *******************************************************************************/
   public void setFormat(String format)
   {
      this.format = format;
   }



   /*******************************************************************************
    ** Fluent setter for format
    *******************************************************************************/
   public SFTPExportConfig withFormat(String format)
   {
      this.format = format;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputValues
    *******************************************************************************/
   public String getInputValues()
   {
      return (this.inputValues);
   }



   /*******************************************************************************
    ** Setter for inputValues
    *******************************************************************************/
   public void setInputValues(String inputValues)
   {
      this.inputValues = inputValues;
   }



   /*******************************************************************************
    ** Fluent setter for inputValues
    *******************************************************************************/
   public SFTPExportConfig withInputValues(String inputValues)
   {
      this.inputValues = inputValues;
      return (this);
   }

}