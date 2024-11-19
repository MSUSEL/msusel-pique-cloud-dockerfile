package evaluation;

import pique.evaluation.DefaultFindingEvaluator;
import pique.evaluation.IEvaluator;
import pique.evaluation.INormalizer;
import pique.evaluation.IUtilityFunction;
import pique.model.Finding;
import pique.model.ModelNode;

import java.math.BigDecimal;
import java.util.Map;

/***
 * this class exists to
 */
public class ValueFinding extends Finding {

    public ValueFinding(String filePath, int lineNumber, int characterNumber, int severity) {
        super(filePath, lineNumber, characterNumber, severity);
    }

    public ValueFinding(BigDecimal value, String name, String description, IEvaluator evaluator, INormalizer normalizer, IUtilityFunction utilityFunction, Map<String, BigDecimal> weights, BigDecimal[] thresholds, Map<String, ModelNode> children, String filePath, int lineNumber, int characterNumber, int severity) {
        super(value, name, description, evaluator, normalizer, utilityFunction, weights, thresholds, children, filePath, lineNumber, characterNumber, severity);
    }

    @Override
    public BigDecimal getValue() {
        return this.value;
    }
}
