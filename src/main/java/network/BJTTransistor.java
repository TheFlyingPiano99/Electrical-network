package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gui.DrawingHelper;
import javafx.scene.canvas.GraphicsContext;
import network.ComponentProperty;
import math.Complex;
import math.Coordinate;
import math.Line;
import math.Matrix;
import math.Vector;

/**
 * Nonlinear BJT (Bipolar Junction Transistor) using Ebers-Moll model.
 * Supports harmonic balance analysis with conductive (non-charge-storage)
 * model.
 * Provides drag-and-drop canvas component for circuit simulation.
 * 
 * Terminal mapping: u = [v_B - v_E, v_B - v_C]^T
 * Output currents: i_C, i_E (emitter and collector currents, positive into
 * device)
 * 
 * @author Copilot
 */
public class BJTTransistor extends Component implements NonlinearDevice {

    private Edge collectorEdge;
    private Edge baseEdge;
    private Edge emitterEdge;

    // Ebers-Moll model parameters
    private double alphaF = 0.99; // Forward current gain
    private double alphaR = 0.1; // Reverse current gain
    private double I_ES = 1e-14; // Saturation current (base-emitter)
    private double I_CS = 1e-14; // Saturation current (base-collector)
    private double V_T = 0.0259; // Thermal voltage (k*T/q at 300K)

    // Terminal nodes
    private ComponentNode collectorNode;
    private ComponentNode baseNode;
    private ComponentNode emitterNode;

    /**
     * Constructor with default NPN parameters.
     */
    public BJTTransistor() {
        super();
    }

    /**
     * Constructor with custom parameters.
     */
    public BJTTransistor(double alphaF, double alphaR, double I_ES, double I_CS, double V_T) {
        super();
        this.alphaF = alphaF;
        this.alphaR = alphaR;
        this.I_ES = I_ES;
        this.I_CS = I_CS;
        this.V_T = V_T;
        validateParameters();
    }

    @Override
    public void validateParameters() {
        if (alphaF < 0 || alphaF >= 1.0) {
            throw new RuntimeException("Invalid alphaF: must be in [0, 1)");
        }
        if (alphaR < 0 || alphaR >= 1.0) {
            throw new RuntimeException("Invalid alphaR: must be in [0, 1)");
        }
        if (I_ES <= 0 || I_CS <= 0 || V_T <= 0) {
            throw new RuntimeException("Invalid saturation currents or thermal voltage");
        }
        // Physical consistency: alphaF * I_ES = alphaR * I_CS
        double expected_I_CS = (alphaF / alphaR) * I_ES;
        if (Math.abs(I_CS - expected_I_CS) > I_CS * 0.1) {
            System.out.println("Warning: I_CS inconsistent with alpha parameters. " +
                    "Expected ~" + expected_I_CS + ", got " + I_CS);
        }
    }

    @Override
    public int getTerminalCount() {
        return 2;
    }

    @Override
    public String getModelName() {
        return "Ebers-Moll NPN BJT";
    }

    @Override
    public TerminalMapping getTerminalMapping() {
        return new BJTTerminalMapping(baseNode, collectorNode, emitterNode,
                getParent());
    }

    @Override
    public void evaluateTimeDomain(double[] terminalVoltages,
            double frequency,
            DeviceEvaluationResult result) {
        result.clear();

        if (terminalVoltages.length != 2) {
            throw new RuntimeException("BJT requires exactly 2 terminal voltages");
        }

        double v_BE = terminalVoltages[0];
        double v_BC = terminalVoltages[1];

        // Compute junction currents
        double I_F = I_ES * (Math.exp(Math.min(v_BE / V_T, 100)) - 1.0);
        double I_R = I_CS * (Math.exp(Math.min(v_BC / V_T, 100)) - 1.0);

        // Terminal currents (positive into device)
        double i_C = alphaF * I_F - I_R;
        double i_E = -I_F + alphaR * I_R;

        // Reduced current mapping: f(u) = [-i_E, -i_C]^T
        result.conductiveCurrents[0] = -i_E;
        result.conductiveCurrents[1] = -i_C;

        // Conductance derivatives
        double g_F = (I_ES / V_T) * Math.exp(Math.min(v_BE / V_T, 100));
        double g_R = (I_CS / V_T) * Math.exp(Math.min(v_BC / V_T, 100));

        // G = df/du
        result.conductanceMatrix[0][0] = g_F;
        result.conductanceMatrix[0][1] = -alphaR * g_R;
        result.conductanceMatrix[1][0] = -alphaF * g_F;
        result.conductanceMatrix[1][1] = g_R;

        // For now, ignore charge storage (capacitance = 0)
        // Will be added in future milestone
    }

    @Override
    public void evaluateDerivatives(double[] terminalVoltages,
            DeviceDerivativeResult result) {
        result.clear();

        if (terminalVoltages.length != 2) {
            throw new RuntimeException("BJT requires exactly 2 terminal voltages");
        }

        double v_BE = terminalVoltages[0];
        double v_BC = terminalVoltages[1];

        // Conductance derivatives
        double g_F = (I_ES / V_T) * Math.exp(Math.min(v_BE / V_T, 100));
        double g_R = (I_CS / V_T) * Math.exp(Math.min(v_BC / V_T, 100));

        // G = df/du
        result.conductanceMatrix[0][0] = g_F;
        result.conductanceMatrix[0][1] = -alphaR * g_R;
        result.conductanceMatrix[1][0] = -alphaF * g_F;
        result.conductanceMatrix[1][1] = g_R;

        // No capacitance contributions yet
    }

    @Override
    public void build() {
        // Generate 3 terminal nodes: Emitter (0), Base (1), Collector (2)
        generateMultiTerminalNodes(3);

        // Access via terminals list
        emitterNode = getTerminal(0);
        baseNode = getTerminal(1);
        collectorNode = getTerminal(2);

        // For backward compatibility with Network.dropComponent positioning:
        // Set input/output to emitter/collector for drag-and-drop placement
        setInput(emitterNode);
        setOutput(collectorNode);

        // Create virtual edges for network representation
        collectorEdge = new Edge();
        baseEdge = new Edge();
        emitterEdge = new Edge();

        getParent().addEdge(collectorEdge);
        getParent().addEdge(baseEdge);
        getParent().addEdge(emitterEdge);

        // Initialize frequency-dependent parameters
        updateFrequencyDependentParameters(getParent().getSimulatedAngularFrequencies());

        // Properties
        setProperties(new HashMap<String, ComponentProperty>());

        ComponentProperty prop = new ComponentProperty();
        prop.editable = true;
        prop.name = "Alpha F:";
        prop.unit = "";
        prop.value = String.valueOf(alphaF);
        getProperties().put("alphaF", prop);

        prop = new ComponentProperty();
        prop.editable = true;
        prop.name = "Alpha R:";
        prop.unit = "";
        prop.value = String.valueOf(alphaR);
        getProperties().put("alphaR", prop);

        prop = new ComponentProperty();
        prop.editable = true;
        prop.name = "I_ES:";
        prop.unit = "A";
        prop.value = String.valueOf(I_ES);
        getProperties().put("I_ES", prop);

        prop = new ComponentProperty();
        prop.editable = true;
        prop.name = "I_CS:";
        prop.unit = "A";
        prop.value = String.valueOf(I_CS);
        getProperties().put("I_CS", prop);

        prop = new ComponentProperty();
        prop.editable = false;
        prop.name = "V_BE:";
        prop.unit = "V";
        prop.value = "0.0";
        getProperties().put("V_BE", prop);

        prop = new ComponentProperty();
        prop.editable = false;
        prop.name = "V_BC:";
        prop.unit = "V";
        prop.value = "0.0";
        getProperties().put("V_BC", prop);
    }

    @Override
    public void destroy() {
        removeMultiTerminalNodes();
        getParent().removeEdge(collectorEdge);
        getParent().removeEdge(baseEdge);
        getParent().removeEdge(emitterEdge);
    }

    @Override
    public void updateFrequencyDependentParameters(ArrayList<Double> simulatedAngularFrequencies) {
        // BJT is nonlinear and frequency-independent at component level
        // Admittance matrix assembly is handled by HarmonicBalanceSolver
    }

    @Override
    public void updateTimeDomainParameters(double totalTimeSec, ArrayList<Double> omegas) {
        // Nonlinear component: no linear impedance
    }

    @Override
    public void updateTimeDomainParametersUsingSpecificFrequencies(
            double totalTimeSec, ArrayList<Double> omegas, ArrayList<Integer> frequencyIndices) {
        // Nonlinear component: no linear impedance
    }

    @Override
    public Vector getFrequencyDomainCurrent() {
        return new Vector(1); // Placeholder
    }

    @Override
    public Vector getFrequencyDomainVoltageDrop() {
        return new Vector(1); // Placeholder
    }

    @Override
    public double getTimeDomainCurrent() {
        return 0.0; // Placeholder
    }

    @Override
    public double getTimeDomainVoltageDrop() {
        return 0.0; // Placeholder
    }

    @Override
    public double getTimeDomainResistance() {
        return 0.0; // Not applicable for nonlinear device
    }

    @Override
    public void save(StringBuilder writer) {
        writer.append("class: ").append(this.getClass().getCanonicalName());
        writer.append("; alphaF: ").append(alphaF);
        writer.append("; alphaR: ").append(alphaR);
        writer.append("; I_ES: ").append(I_ES);
        writer.append("; I_CS: ").append(I_CS);
        writer.append("; V_T: ").append(V_T);
        writer.append("; collectorPos: ").append(String.format("[%d, %d]",
                collectorNode.getPos().x, collectorNode.getPos().y));
        writer.append("; basePos: ").append(String.format("[%d, %d]",
                baseNode.getPos().x, baseNode.getPos().y));
        writer.append("; emitterPos: ").append(String.format("[%d, %d]",
                emitterNode.getPos().x, emitterNode.getPos().y));
        writer.append("\n");
    }

    @Override
    public void load(String[] pairs) {
        // Parse saved parameters
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length < 2)
                continue;
            String key = kv[0].trim();
            String value = kv[1].trim();

            try {
                if ("alphaF".equals(key))
                    alphaF = Double.parseDouble(value);
                else if ("alphaR".equals(key))
                    alphaR = Double.parseDouble(value);
                else if ("I_ES".equals(key))
                    I_ES = Double.parseDouble(value);
                else if ("I_CS".equals(key))
                    I_CS = Double.parseDouble(value);
                else if ("V_T".equals(key))
                    V_T = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                // Skip malformed values
            }
        }
        validateParameters();
        updatePropertyView(true);
    }

    @Override
    public void updatePropertyModel() {
        synchronized (getParent().getMutexObj()) {
            try {
                Map<String, ComponentProperty> props = getProperties();
                alphaF = Double.parseDouble(props.get("alphaF").value);
                alphaR = Double.parseDouble(props.get("alphaR").value);
                I_ES = Double.parseDouble(props.get("I_ES").value);
                I_CS = Double.parseDouble(props.get("I_CS").value);
                validateParameters();
            } catch (Exception e) {
                // Revert on parse error
            }
        }
    }

    @Override
    public void updatePropertyView(boolean updateEditable) {
        if (updateEditable) {
            setProperty("alphaF", () -> alphaF);
            setProperty("alphaR", () -> alphaR);
            setProperty("I_ES", () -> I_ES);
            setProperty("I_CS", () -> I_CS);
        }
        // V_BE and V_BC would be updated from simulation results
        setProperty("V_BE", () -> 0.0); // Placeholder
        setProperty("V_BC", () -> 0.0); // Placeholder
    }

    @Override
    public void draw(GraphicsContext ctx) {
        updatePropertyView(false);
        List<Line> lines = new ArrayList<Line>();

        // Construction:
        float defaultSize = getDEFAULT_SIZE();
        lines.add(new Line(0.0f, 0.0f, defaultSize * 0.2f, 0.0f));

        // Circle:
        int resolution = 32;
        for (int i = 0; i < resolution; i++) {
            double angle0 = i / (double) resolution * 2 * Math.PI;
            double angle1 = (i + 1) / (double) resolution * 2 * Math.PI;
            float x0 = (float) Math.cos(angle0) * 0.3f + 0.5f;
            float y0 = (float) Math.sin(angle0) * 0.3f;
            float x1 = (float) Math.cos(angle1) * 0.3f + 0.5f;
            float y1 = (float) Math.sin(angle1) * 0.3f;
            lines.add(new Line(defaultSize * x0, defaultSize * y0, defaultSize * x1, defaultSize * y1));
        }

        // waveform:
        lines.add(new Line(defaultSize * 0.2f, 0.0f, defaultSize * 0.65f, -defaultSize * 0.1f));
        lines.add(new Line(defaultSize * 0.65f, -defaultSize * 0.1f, defaultSize * 0.65f, 0.0f));
        lines.add(new Line(defaultSize * 0.65f, 0.f, defaultSize * 0.8f, 0.0f));

        lines.add(new Line(defaultSize * 0.8f, 0.0f, defaultSize, 0.0f));

        // [+] sign
        lines.add(new Line(
                (5.0f / 6.0f * defaultSize) - (1.0f / 15.0f * defaultSize),
                -1.0f / 4.0f * defaultSize,
                (5.0f / 6.0f * defaultSize) + (1.0f / 15.0f * defaultSize),
                -1.0f / 4.0f * defaultSize));
        lines.add(new Line(
                (5.0f / 6.0f * defaultSize),
                -1.0f / 4.0f * defaultSize - (1.0f / 15.0f * defaultSize),
                (5.0f / 6.0f * defaultSize),
                -1.0f / 4.0f * defaultSize + (1.0f / 15.0f * defaultSize)));

        // [-] sign
        lines.add(new Line(
                (1.0f / 6.0f * defaultSize) - (1.0f / 15.0f * defaultSize),
                -1.0f / 4.0f * defaultSize,
                (1.0f / 6.0f * defaultSize) + (1.0f / 15.0f * defaultSize),
                -1.0f / 4.0f * defaultSize));

        // call drawShape
        DrawingHelper.drawShape(ctx,
                getInput().getPos(),
                getOutput().getPos(),
                lines,
                defaultSize,
                getParent().isThisSelected(this),
                getCurrentVisualisationOffset(),
                getParent().isValid(),
                (float) 0,
                (float) 0);
    }

    @Override
    public void disconnectGraphRepresentation() {
        // Virtual edges don't need disconnection in the same way
    }

    @Override
    public void reset() {
        // Clear stored state if any
    }

    @Override
    public String toString() {
        return String.format("BJTTransistor[alphaF=%.3f, alphaR=%.3f, I_ES=%.2e, I_CS=%.2e]",
                alphaF, alphaR, I_ES, I_CS);
    }

    @Override
    public BJTTransistor clone() {
        try {
            BJTTransistor clone = (BJTTransistor) super.clone();
            clone.alphaF = this.alphaF;
            clone.alphaR = this.alphaR;
            clone.I_ES = this.I_ES;
            clone.I_CS = this.I_CS;
            clone.V_T = this.V_T;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported for BJT");
        }
    }

    // Getters/Setters
    public double getAlphaF() {
        return alphaF;
    }

    public void setAlphaF(double val) {
        alphaF = val;
        validateParameters();
    }

    public double getAlphaR() {
        return alphaR;
    }

    public void setAlphaR(double val) {
        alphaR = val;
        validateParameters();
    }

    public double getI_ES() {
        return I_ES;
    }

    public void setI_ES(double val) {
        I_ES = val;
        validateParameters();
    }

    public double getI_CS() {
        return I_CS;
    }

    public void setI_CS(double val) {
        I_CS = val;
        validateParameters();
    }

    public double getV_T() {
        return V_T;
    }

    public void setV_T(double val) {
        V_T = val;
        validateParameters();
    }

    public ComponentNode getCollectorNode() {
        return collectorNode;
    }

    public ComponentNode getBaseNode() {
        return baseNode;
    }

    public ComponentNode getEmitterNode() {
        return emitterNode;
    }

    /**
     * Inner class implementing TerminalMapping for this BJT.
     */
    private static class BJTTerminalMapping implements TerminalMapping {
        private final ComponentNode baseNode;
        private final ComponentNode collectorNode;
        private final ComponentNode emitterNode;
        private final Network network;
        private Matrix mappingMatrix;

        BJTTerminalMapping(ComponentNode base, ComponentNode collector, ComponentNode emitter, Network network) {
            this.baseNode = base;
            this.collectorNode = collector;
            this.emitterNode = emitter;
            this.network = network;
            buildMappingMatrix();
        }

        private void buildMappingMatrix() {
            // Mapping: u = [v_B - v_E, v_B - v_C]^T
            int nodeCount = network.getVertices().size();
            mappingMatrix = new Matrix(2, nodeCount);

            int baseIdx = network.getVertices().indexOf(baseNode.getVertexBinding());
            int collectorIdx = network.getVertices().indexOf(collectorNode.getVertexBinding());
            int emitterIdx = network.getVertices().indexOf(emitterNode.getVertexBinding());

            // Row 0: u_0 = v_B - v_E
            mappingMatrix.setAt(0, baseIdx, new Complex(1, 0));
            mappingMatrix.setAt(0, emitterIdx, new Complex(-1, 0));

            // Row 1: u_1 = v_B - v_C
            mappingMatrix.setAt(1, baseIdx, new Complex(1, 0));
            mappingMatrix.setAt(1, collectorIdx, new Complex(-1, 0));
        }

        @Override
        public Matrix getMappingMatrix() {
            return mappingMatrix;
        }

        @Override
        public int getTerminalDimension() {
            return 2;
        }

        @Override
        public int[] getInvolvedNodes() {
            return new int[] {
                    network.getVertices().indexOf(baseNode.getVertexBinding()),
                    network.getVertices().indexOf(collectorNode.getVertexBinding()),
                    network.getVertices().indexOf(emitterNode.getVertexBinding())
            };
        }
    }
}
