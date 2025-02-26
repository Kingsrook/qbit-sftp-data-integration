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
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildJoin;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildRecordListWidget;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildTable;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;


/*******************************************************************************
 ** QRecord Entity for SFTPConnection table
 *******************************************************************************/
@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = SFTPConnection.TableCustomizer.class,
   childTables = {
      @ChildTable(childTableEntityClass = SFTPImportConfig.class, joinFieldName = "sftpConnectionId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(enabled = true, label = "Import configs")),
      @ChildTable(childTableEntityClass = SFTPExportConfig.class, joinFieldName = "sftpConnectionId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(enabled = true, label = "Export configs"))
   }
)
public class SFTPConnection extends QRecordEntity
{
   public static final String TABLE_NAME = "sftpConnection";



   /***************************************************************************
    **
    ***************************************************************************/
   public static class TableCustomizer implements MetaDataCustomizerInterface<QTableMetaData>
   {
      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public QTableMetaData customizeMetaData(QInstance qInstance, QTableMetaData table) throws QException
      {
         table
            .withLabel("SFTP Connection")
            .withIcon(new QIcon().withName("outlet"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("name")
            .withUniqueKey(new UniqueKey("name"))
            .withSection(SectionFactory.defaultT1("id", "name"))
            .withSection(SectionFactory.defaultT2("username", "password", "hostname", "port", "basePath", "isActive"))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         table.getField("password")
            .withType(QFieldType.PASSWORD)
            .withFieldAdornment(new FieldAdornment(AdornmentType.REVEAL));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;

   @QField(isRequired = true, maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String name;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String username;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String password;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String hostname;

   @QField(defaultValue = "22")
   private Integer port;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String basePath;

   @QField(isRequired = true, defaultValue = "true")
   private Boolean isActive;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public SFTPConnection()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public SFTPConnection(QRecord record)
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
   public SFTPConnection withId(Integer id)
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
   public SFTPConnection withCreateDate(Instant createDate)
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
   public SFTPConnection withModifyDate(Instant modifyDate)
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
   public SFTPConnection withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for username
    *******************************************************************************/
   public String getUsername()
   {
      return (this.username);
   }



   /*******************************************************************************
    ** Setter for username
    *******************************************************************************/
   public void setUsername(String username)
   {
      this.username = username;
   }



   /*******************************************************************************
    ** Fluent setter for username
    *******************************************************************************/
   public SFTPConnection withUsername(String username)
   {
      this.username = username;
      return (this);
   }



   /*******************************************************************************
    ** Getter for password
    *******************************************************************************/
   public String getPassword()
   {
      return (this.password);
   }



   /*******************************************************************************
    ** Setter for password
    *******************************************************************************/
   public void setPassword(String password)
   {
      this.password = password;
   }



   /*******************************************************************************
    ** Fluent setter for password
    *******************************************************************************/
   public SFTPConnection withPassword(String password)
   {
      this.password = password;
      return (this);
   }



   /*******************************************************************************
    ** Getter for hostname
    *******************************************************************************/
   public String getHostname()
   {
      return (this.hostname);
   }



   /*******************************************************************************
    ** Setter for hostname
    *******************************************************************************/
   public void setHostname(String hostname)
   {
      this.hostname = hostname;
   }



   /*******************************************************************************
    ** Fluent setter for hostname
    *******************************************************************************/
   public SFTPConnection withHostname(String hostname)
   {
      this.hostname = hostname;
      return (this);
   }



   /*******************************************************************************
    ** Getter for port
    *******************************************************************************/
   public Integer getPort()
   {
      return (this.port);
   }



   /*******************************************************************************
    ** Setter for port
    *******************************************************************************/
   public void setPort(Integer port)
   {
      this.port = port;
   }



   /*******************************************************************************
    ** Fluent setter for port
    *******************************************************************************/
   public SFTPConnection withPort(Integer port)
   {
      this.port = port;
      return (this);
   }



   /*******************************************************************************
    ** Getter for basePath
    *******************************************************************************/
   public String getBasePath()
   {
      return (this.basePath);
   }



   /*******************************************************************************
    ** Setter for basePath
    *******************************************************************************/
   public void setBasePath(String basePath)
   {
      this.basePath = basePath;
   }



   /*******************************************************************************
    ** Fluent setter for basePath
    *******************************************************************************/
   public SFTPConnection withBasePath(String basePath)
   {
      this.basePath = basePath;
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
   public SFTPConnection withIsActive(Boolean isActive)
   {
      this.isActive = isActive;
      return (this);
   }

}
