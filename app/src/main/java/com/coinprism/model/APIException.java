/*
 * Copyright (c) 2014 Flavien Charlon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.coinprism.model;

/**
 * Represents an exception thrown by the Coinprism API.
 */
public class APIException extends Exception
{
    private final String errorCode;
    private final String subCode;

    public APIException(String errorCode, String subCode)
    {
        this.errorCode = errorCode;
        this.subCode = subCode;
    }

    public String getErrorCode()
    {
        return errorCode;
    }

    public String getSubCode()
    {
        return subCode;
    }
}
