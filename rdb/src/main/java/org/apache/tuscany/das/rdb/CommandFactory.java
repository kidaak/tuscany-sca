/**
 *
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tuscany.das.rdb;

import java.io.IOException;
import java.io.InputStream;

/**
 * A CommandFactory produces {@link Command} and {@link ApplyChangesCommand}
 * instances.
 * 
 * 
 */
public interface CommandFactory {

	/**
	 * Creates a Command based on the provided SQL statement
	 * 
	 * @param sql
	 *            The SQL statement
	 * @return returns a Command instance
	 */
	public Command createCommand(String sql);

	/**
	 * Creates a Command based on the provided SQL statement and configuration
	 * 
	 * @param sql
	 *            The SQL statement
	 * @param mappingModel
	 *            The congiguration as XML file stream
	 * @return returns a COmmand instance
	 */
	public Command createCommand(String sql, InputStream mappingModel);

	/**
	 * Creates an {@linkApplyChangesCommand} instance
	 * @return Returns the ApplyChangesCommand instance
	 */
	public ApplyChangesCommand createApplyChangesCommand();

	/**
	 * Creates an {@linkApplyChangesCommand} instance with the provided configuration
	 * @param mappingModel The provided configuration as a stream over an xml file
	 * @return Returns an ApplyChangesCOmmand in stance
	 * @throws IOException
	 * TODO - Either remove this throws clause or add it to createCommand
	 */
	public ApplyChangesCommand createApplyChangesCommand(
			InputStream mappingModel) throws IOException;


}
