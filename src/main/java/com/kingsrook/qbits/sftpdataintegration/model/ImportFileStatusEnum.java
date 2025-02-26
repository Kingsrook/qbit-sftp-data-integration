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


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;
import static com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType.ChipValues.iconAndColorValues;


/*******************************************************************************
 ** ImportFileStatusEnum - possible value enum
 *******************************************************************************/
@QMetaDataProducingPossibleValueEnum
public enum ImportFileStatusEnum implements PossibleValueEnum<Integer>
{
   PENDING(1, "Pending"),
   PROCESSING(2, "Processing"),
   COMPLETE(3, "Complete"),
   ERROR(4, "Error");

   private final Integer id;
   private final String  label;

   public static final String NAME = "ImportFileStatusEnum";



   /***************************************************************************
    *
    ***************************************************************************/
   public static void addChipAdornmentToField(QFieldMetaData field)
   {
      field.withFieldAdornment(new FieldAdornment(AdornmentType.CHIP)
         .withValues(iconAndColorValues(PENDING, "pending", AdornmentType.ChipValues.COLOR_DEFAULT))
         .withValues(iconAndColorValues(PROCESSING, "double_arrow", AdornmentType.ChipValues.COLOR_INFO))
         .withValues(iconAndColorValues(COMPLETE, "done", AdornmentType.ChipValues.COLOR_SUCCESS))
         .withValues(iconAndColorValues(ERROR, "error", AdornmentType.ChipValues.COLOR_ERROR)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   ImportFileStatusEnum(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    **
    *******************************************************************************/
   public static ImportFileStatusEnum getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(ImportFileStatusEnum value : ImportFileStatusEnum.values())
      {
         if(Objects.equals(value.id, id))
         {
            return (value);
         }
      }

      return (null);
   }



   /*******************************************************************************
    ** Getter for id
    **
    *******************************************************************************/
   public Integer getId()
   {
      return id;
   }



   /*******************************************************************************
    ** Getter for label
    **
    *******************************************************************************/
   public String getLabel()
   {
      return label;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Integer getPossibleValueId()
   {
      return (getId());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getPossibleValueLabel()
   {
      return (getLabel());
   }
}
