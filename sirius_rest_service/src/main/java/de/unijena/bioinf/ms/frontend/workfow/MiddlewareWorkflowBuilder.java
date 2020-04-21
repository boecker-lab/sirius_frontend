package de.unijena.bioinf.ms.frontend.workfow;

import de.unijena.bioinf.ms.frontend.subtools.RootOptions;
import de.unijena.bioinf.ms.frontend.subtools.config.DefaultParameterConfigLoader;
import de.unijena.bioinf.ms.frontend.subtools.middleware.MiddlewareAppOptions;
import de.unijena.bioinf.ms.frontend.workflow.InstanceBufferFactory;
import de.unijena.bioinf.ms.frontend.workflow.WorkflowBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MiddlewareWorkflowBuilder<R extends RootOptions<?, ?, ?>> extends WorkflowBuilder<R> {
    public MiddlewareWorkflowBuilder(@NotNull R rootOptions, @NotNull DefaultParameterConfigLoader configOptionLoader, InstanceBufferFactory<?> bufferFactory) throws IOException {
        super(rootOptions, configOptionLoader, bufferFactory);
    }

    @Override
    protected Object[] standaloneTools() {
        ArrayList<Object> it = new ArrayList<>(Arrays.asList(super.standaloneTools()));
        it.add(new MiddlewareAppOptions());
        return it.toArray(Object[]::new);
    }
}
