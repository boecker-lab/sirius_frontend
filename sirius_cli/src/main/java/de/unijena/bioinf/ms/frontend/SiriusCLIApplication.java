package de.unijena.bioinf.ms.frontend;

import de.unijena.bioinf.jjobs.JobManager;
import de.unijena.bioinf.ms.frontend.core.ApplicationCore;
import de.unijena.bioinf.ms.frontend.io.projectspace.InstanceFactory;
import de.unijena.bioinf.ms.frontend.subtools.RootOptionsCLI;
import de.unijena.bioinf.ms.frontend.subtools.config.DefaultParameterConfigLoader;
import de.unijena.bioinf.ms.frontend.workflow.WorkflowBuilder;
import de.unijena.bioinf.net.ProxyManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;


public class SiriusCLIApplication {
    protected static Run RUN = null;


    public static void main(String[] args) {
        try {
            configureShutDownHook(() -> {
            });
            run(args, () -> {
                final DefaultParameterConfigLoader configOptionLoader = new DefaultParameterConfigLoader();
                return new WorkflowBuilder<>(new RootOptionsCLI(configOptionLoader, new InstanceFactory.Default()), configOptionLoader);
            });
        } finally {

        }
    }

    public static void configureShutDownHook(@NotNull final Runnable additionalActions) {
        //shut down hook to clean up when sirius is shutting down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ApplicationCore.DEFAULT_LOGGER.info("CLI shut down hook: SIRIUS is cleaning up threads and shuts down...");
            try {
                if (SiriusCLIApplication.RUN != null)
                    SiriusCLIApplication.RUN.cancel();
                additionalActions.run();
                JobManager.shutDownNowAllInstances();
                ApplicationCore.WEB_API.unregisterClientAndDeleteJobsFromServer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                ProxyManager.disconnect();
                ApplicationCore.cite();
            }
        }));
    }

    public static void run(String[] args, WorkFlowSupplier supplier) {
        try {
            if (RUN != null)
                throw new IllegalStateException("Aplication can only run Once!");
            RUN = new Run(supplier.make());
            if (RUN.parseArgs(args))
                RUN.compute();
        } catch (Throwable e) {
            LoggerFactory.getLogger(SiriusCLIApplication.class).error("Unexpected Error!", e);
        }
    }


    @FunctionalInterface
    public interface WorkFlowSupplier {
        WorkflowBuilder<?> make() throws Exception;
    }
}
