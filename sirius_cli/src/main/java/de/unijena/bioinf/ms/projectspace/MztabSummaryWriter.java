package de.unijena.bioinf.ms.projectspace;

import de.unijena.bioinf.ms.projectspace.mztab.MztabMExporter;
import de.unijena.bioinf.sirius.ExperimentResult;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MztabSummaryWriter implements SummaryWriter {
    @Override
    public void writeSummary(Iterable<ExperimentResult> experiments, DirectoryWriter writer) {
        MztabMExporter mztabMExporter = new MztabMExporter();
        for (ExperimentResult er : experiments)
            mztabMExporter.addExperiment(er, er.getResults());
        try {
            writer.write(FingerIdLocations.WORKSPACE_SUMMARY.fileName(), mztabMExporter::write);
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Error when writing mztab Summary");
        }
    }
}
