/*
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
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sejda.core.service;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.Test;
import org.sejda.core.notification.context.GlobalNotificationContext;
import org.sejda.model.exception.TaskCancelledException;
import org.sejda.model.notification.EventListener;
import org.sejda.model.notification.event.TaskExecutionFailedEvent;
import org.sejda.model.optimization.Optimization;
import org.sejda.model.parameter.OptimizeParameters;
import org.sejda.model.pdf.PdfVersion;
import org.sejda.model.task.CancellationOption;
import org.sejda.sambox.cos.COSDictionary;
import org.sejda.sambox.cos.COSName;
import org.sejda.sambox.pdmodel.PDPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OptimizeTaskTest extends BaseTaskTest<OptimizeParameters> {
    private static Logger LOG = LoggerFactory.getLogger(OptimizeTaskTest.class);

    private OptimizeParameters parameters;

    private void setUpParameters() throws IOException {
        parameters = new OptimizeParameters();
        parameters.addOptimization(Optimization.COMPRESS_IMAGES);
        parameters.addOptimization(Optimization.DISCARD_METADATA);
        parameters.setImageQuality(0.65f);
        parameters.setImageDpi(72);
        parameters.setVersion(PdfVersion.VERSION_1_6);
        testContext.directoryOutputTo(parameters);
    }

    private long sizeOfResult(Path p) {
        try {
            return Files.size(p) / 1000;
        } catch (IOException e) {
            LOG.error("Test failed", e);
            fail(e.getMessage());
        }
        return 0;
    }

    @Test
    public void testBasics() throws IOException {
        setUpParameters();
        parameters.addSource(customInput("pdf/unoptimized.pdf"));
        execute(parameters);
        testContext.assertTaskCompleted();
        testContext.forEachRawOutput(p -> assertThat(sizeOfResult(p), is(lessThan(551L))));

    }

    @Test
    public void testRepeatedImages() throws IOException {
        setUpParameters();
        parameters.addSource(customInput("pdf/test_optimize_repeated_images.pdf"));
        execute(parameters);
        testContext.assertTaskCompleted();
        testContext.forEachRawOutput(p -> assertThat(sizeOfResult(p), is(lessThan(468L))));
    }

    @Test
    public void testAlreadyCompressed() throws IOException {
        setUpParameters();
        parameters.setImageQuality(1f);
        parameters.addSource(customInput("pdf/draw_w_transparency.pdf"));
        execute(parameters);
        testContext.assertTaskCompleted();
    }

    @Test
    public void testRemoveFonts() throws IOException {
        setUpParameters();
        parameters.addSource(customInput("pdf/unused_fonts.pdf"));
        parameters.addOptimization(Optimization.DISCARD_UNUSED_RESOURCES);
        execute(parameters);
        testContext.assertTaskCompleted();
        testContext.forEachRawOutput(p -> assertThat(sizeOfResult(p), is(lessThan(1000L))));
        testContext.forPdfOutput(d -> {
            for (PDPage p : d.getPages()) {
                assertEquals(1,
                        p.getResources().getCOSObject().getDictionaryObject(COSName.FONT, COSDictionary.class).size());
            }
        });
    }

    @Test
    public void testCancellation() throws Throwable {
        setUpParameters();
        ExecutorService executor = Executors.newCachedThreadPool();
        parameters.addSource(customInput("pdf/test_optimize_repeated_images.pdf"));
        CancellationOption cancellationOption = new CancellationOption();

        CancellationListener cancellationListener = new CancellationListener();
        GlobalNotificationContext.getContext().addListener(cancellationListener);

        Future<?> future = executor.submit(() -> {
            execute(parameters, cancellationOption);
        });

        // wait for the task to start
        while (!cancellationOption.isCancellable()) {
            Thread.sleep(100);
        }

        // request for it to cancel and wait 2 seconds to complete
        cancellationOption.requestCancel();
        future.get(2, TimeUnit.SECONDS);

        assertThat("Task was cancelled", cancellationListener.cancelled.booleanValue(), is(true));
    }

    private static class CancellationListener implements EventListener<TaskExecutionFailedEvent> {

        MutableBoolean cancelled = new MutableBoolean(false);

        @Override
        public void onEvent(TaskExecutionFailedEvent event) {
            if (event.getFailingCause() instanceof TaskCancelledException) {
                cancelled.setTrue();
            }
        }
    }
}
