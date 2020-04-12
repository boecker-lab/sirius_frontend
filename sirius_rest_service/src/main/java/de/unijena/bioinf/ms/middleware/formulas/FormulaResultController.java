package de.unijena.bioinf.ms.middleware.formulas;

import de.unijena.bioinf.ChemistryBase.ms.ft.FTree;
import de.unijena.bioinf.ms.annotations.DataAnnotation;
import de.unijena.bioinf.ms.middleware.BaseApiController;
import de.unijena.bioinf.ms.middleware.SiriusContext;
import de.unijena.bioinf.projectspace.FormulaResultId;
import de.unijena.bioinf.projectspace.FormulaScoring;
import de.unijena.bioinf.projectspace.SiriusProjectSpace;
import de.unijena.bioinf.projectspace.sirius.CompoundContainer;
import de.unijena.bioinf.projectspace.sirius.FormulaResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/projects/{pid}/compounds/{cid}")
public class FormulaResultController extends BaseApiController {

    @Autowired
    public FormulaResultController(SiriusContext context) {
        super(context);
    }

    @GetMapping(value = "/formulas", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<FormulaId> getFormulaIds(@PathVariable String pid, @PathVariable String cid, @RequestParam(required = false) boolean includeFormulaScores) {
        SiriusProjectSpace space = projectSpace(pid);
        return space.findCompound(cid).map(ccId -> {
            try {
                return space.getCompound(ccId);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).map(con -> con.getResults().values().stream().map(frId -> {
            try{
                return space.getFormulaResult(frId, FormulaScoring.class);
            } catch (IOException e) {
                return null;
            }
        }).map(fr -> {
            FormulaId formulaId = new FormulaId(fr.getId());
            if(includeFormulaScores){
                fr.getAnnotation(FormulaScoring.class).ifPresent(fs -> formulaId.setResultScores(new FormulaResultScores(fs)));
            }
            return formulaId;
        }).collect(Collectors.toList())).orElse(Collections.emptyList());
    }

    @GetMapping(value = "/formulas/{fid}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public FormulaId getFormulaResult(@PathVariable String pid, @PathVariable String cid, @PathVariable String fid) {
        SiriusProjectSpace space = projectSpace(pid);
        return getCompound(space, cid).map(c ->
                c.findResult(fid).map(ffid -> {
                    try {
                        return space.getFormulaResult(ffid, FormulaScoring.class);
                    } catch (IOException e) {
                        return null;
                    }
                }).orElse(null)).
                map(fr -> {
                    FormulaId formulaId = new FormulaId(fr.getId());
                    fr.getAnnotation(FormulaScoring.class).ifPresent(fs -> formulaId.setResultScores(new FormulaResultScores(fs)));
                    return formulaId;
        }).orElse(null);
    }

    @RequestMapping(value = "formulas/{fid}/tree", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public FTree getFragmentationTree(@PathVariable String pid, @PathVariable String cid, @PathVariable String fid){
        SiriusProjectSpace projectSpace = super.projectSpace(pid);
        Optional<FormulaResult> fResult = this.getCompound(projectSpace,cid).map(cc -> {
            Optional<FormulaResultId> fResultId = cc.findResult(fid);
            return fResultId.map(frId -> {
                try {
                    return projectSpace.getFormulaResult(frId, FTree.class);
                } catch (IOException e) {
                    return null;
                }
            }).orElse(null);
        });
        FTree fTree = fResult.map(fr -> fr.getAnnotation(FTree.class).orElse(null)).orElse(null);
        return fTree;
        // The FTree object needs an extra class for JSON. It can't be displayed well. Nevertheless, it works with attributes of the FTree object.
    }

    private Optional<CompoundContainer> getCompound(SiriusProjectSpace space, String cid, Class<? extends DataAnnotation>... components) {
        return space.findCompound(cid).map(ccId -> {
            try {
                return space.getCompound(ccId, components);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }


}

