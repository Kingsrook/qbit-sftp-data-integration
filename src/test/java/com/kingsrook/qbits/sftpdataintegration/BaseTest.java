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


import java.io.File;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.common.TimeZonePossibleValueSourceMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.audits.AuditLevel;
import com.kingsrook.qqq.backend.core.model.metadata.audits.QAuditRules;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.ProvidedOrSuppliedTableConfig;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.simple.SimpleSchedulerMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.TablesPossibleValueSourceMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.savedbulkloadprofiles.SavedBulkLoadProfileMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReportsMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJobsMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryBackendModule;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.processes.tracing.LoggingProcessTracer;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemBackendMetaData;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;


/*******************************************************************************
 **
 *******************************************************************************/
public class BaseTest
{
   public static final String MEMORY_BACKEND_NAME                 = "memory";
   public static final String SOURCE_FILESYSTEM_BACKEND_NAME      = "sourceFilesystem";
   public static final String DESTINATION_FILESYSTEM_BACKEND_NAME = "destinationFilesystem";
   public static final String STAGING_FILESYSTEM_BACKEND_NAME     = "stagingFilesystem";

   public static final String FILESYSTEM_ROOT_PATH = "/tmp/qbit-sftp-data-integration-test";



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void baseBeforeEach() throws Exception
   {
      FileUtils.deleteDirectory(new File(FILESYSTEM_ROOT_PATH));

      QInstance qInstance = defineQInstance();
      new QInstanceValidator().validate(qInstance);
      QContext.init(qInstance, new QSession());

      MemoryRecordStore.fullReset();
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private QInstance defineQInstance() throws QException
   {
      /////////////////////////////////////
      // basic definition of an instance //
      /////////////////////////////////////
      QInstance qInstance = new QInstance();
      qInstance.setAuthentication(new QAuthenticationMetaData().withType(QAuthenticationType.FULLY_ANONYMOUS));
      qInstance.addBackend(new QBackendMetaData().withBackendType(MemoryBackendModule.class).withName(MEMORY_BACKEND_NAME));

      //////////////////////////////////////////////
      // add a scheduler and scheduled job tables //
      //////////////////////////////////////////////
      qInstance.addScheduler(new SimpleSchedulerMetaData().withName("scheduler"));
      new ScheduledJobsMetaDataProvider().defineAll(qInstance, MEMORY_BACKEND_NAME, null);
      qInstance.addPossibleValueSource(new TimeZonePossibleValueSourceMetaDataProvider().produce());

      /////////////////////////////
      // add filesystem backends //
      /////////////////////////////
      qInstance.addBackend(new FilesystemBackendMetaData().withName(SOURCE_FILESYSTEM_BACKEND_NAME).withBasePath(FILESYSTEM_ROOT_PATH + "/source"));
      qInstance.addBackend(new FilesystemBackendMetaData().withName(DESTINATION_FILESYSTEM_BACKEND_NAME).withBasePath(FILESYSTEM_ROOT_PATH + "/destination"));
      qInstance.addBackend(new FilesystemBackendMetaData().withName(STAGING_FILESYSTEM_BACKEND_NAME).withBasePath(FILESYSTEM_ROOT_PATH + "/staging"));

      /////////////////////////////////////
      // more dependencies for this qbit //
      /////////////////////////////////////
      new SavedBulkLoadProfileMetaDataProvider().defineAll(qInstance, MEMORY_BACKEND_NAME, null);
      qInstance.addPossibleValueSource(TablesPossibleValueSourceMetaDataProvider.defineTablesPossibleValueSource(qInstance));
      new SavedReportsMetaDataProvider().defineAll(qInstance, MEMORY_BACKEND_NAME, DESTINATION_FILESYSTEM_BACKEND_NAME, null);

      ////////////////////////
      // configure the qbit //
      ////////////////////////
      SFTPDataIntegrationQBitConfig sftpDataIntegrationQBitConfig = new SFTPDataIntegrationQBitConfig()
         .withSourceFileTableConfig(ProvidedOrSuppliedTableConfig.provideTableUsingBackendNamed(SOURCE_FILESYSTEM_BACKEND_NAME))
         .withDestinationFileTableConfig(ProvidedOrSuppliedTableConfig.provideTableUsingBackendNamed(DESTINATION_FILESYSTEM_BACKEND_NAME))
         .withStagingFileTableConfig(ProvidedOrSuppliedTableConfig.provideTableUsingBackendNamed(STAGING_FILESYSTEM_BACKEND_NAME))
         .withSchedulerName("scheduler")
         .withProcessTracerCodeReference(new QCodeReference(LoggingProcessTracer.class))
         .withTableMetaDataCustomizer((i, table) ->
         {
            if(table.getBackendName() == null)
            {
               table.setBackendName(MEMORY_BACKEND_NAME);
            }

            return (table);
         });

      //////////////////////
      // produce our qbit //
      //////////////////////
      new SFTPDataIntegrationQBitProducer()
         .withSftpDataIntegrationQBitConfig(sftpDataIntegrationQBitConfig)
         .produce(qInstance);

      ///////////////////////////////////////////
      // turn off audits (why on by default??) //
      ///////////////////////////////////////////
      qInstance.getTables().values().forEach(t -> t.setAuditRules(new QAuditRules().withAuditLevel(AuditLevel.NONE)));
      return qInstance;
   }

}
