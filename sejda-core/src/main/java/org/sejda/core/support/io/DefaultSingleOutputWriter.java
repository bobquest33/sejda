/*
 * Created on 29/ago/2010
 *
 * Copyright 2010 by Andrea Vacondio (andrea.vacondio@gmail.com).
 * 
 * This file is part of the Sejda source code
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sejda.core.support.io;

import java.io.IOException;

import org.sejda.core.support.io.model.PopulatedFileOutput;
import org.sejda.model.output.ExistingOutputPolicy;
import org.sejda.model.output.StreamTaskOutput;
import org.sejda.model.task.TaskExecutionContext;

/**
 * Single writer default implementation.
 * 
 * @author Andrea Vacondio
 * 
 */
class DefaultSingleOutputWriter extends BaseOutputWriter implements SingleOutputWriter {

    DefaultSingleOutputWriter(ExistingOutputPolicy existingOutputPolicy, TaskExecutionContext executionContext) {
        super(existingOutputPolicy, executionContext);
    }

    @Override
    public void dispatch(StreamTaskOutput output) throws IOException {
        OutputWriterHelper.copyToStream(multipleFiles.values().iterator().next(), output.getDestination());
    }

    @Override
    public void setOutput(PopulatedFileOutput fileOutput) {
        add(fileOutput);
    }

}
