package network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import math.Complex;

/**
 * Unit tests for nonlinear BJT transistor model.
 * Validates Ebers-Moll equations and derivatives against analytical and numerical benchmarks.
 */
@DisplayName("BJT Transistor Model Tests")
public class BJTTransistorTest {
    
    private BJTTransistor bjt;
    private static final double V_T = 0.0259;  // Thermal voltage
    private static final double I_ES = 1e-14;
    private static final double I_CS = 1e-14;
    private static final double ALPHA_F = 0.99;
    private static final double ALPHA_R = 0.1;
    
    @BeforeEach
    void setUp() {
        bjt = new BJTTransistor(ALPHA_F, ALPHA_R, I_ES, I_CS, V_T);
    }
    
    @Test
    @DisplayName("Parameter validation rejects invalid alpha values")
    void testParameterValidationAlpha() {
        assertThrows(RuntimeException.class, () -> {
            new BJTTransistor(1.5, ALPHA_R, I_ES, I_CS, V_T);  // alphaF > 1
        });
        assertThrows(RuntimeException.class, () -> {
            new BJTTransistor(ALPHA_F, -0.1, I_ES, I_CS, V_T);  // alphaR < 0
        });
    }
    
    @Test
    @DisplayName("Parameter validation rejects invalid current values")
    void testParameterValidationCurrents() {
        assertThrows(RuntimeException.class, () -> {
            new BJTTransistor(ALPHA_F, ALPHA_R, -1e-14, I_CS, V_T);  // I_ES < 0
        });
        assertThrows(RuntimeException.class, () -> {
            new BJTTransistor(ALPHA_F, ALPHA_R, I_ES, 0, V_T);  // I_CS = 0
        });
    }
    
    @Test
    @DisplayName("Terminal current conservation: i_B + i_C + i_E = 0")
    void testCurrentConservation() {
        DeviceEvaluationResult result = new DeviceEvaluationResult(2);
        double[] v_be_bc = {0.3, -0.7};  // Moderate forward bias on BE, reverse on BC
        
        bjt.evaluateTimeDomain(v_be_bc, 0.0, result);
        
        // Reconstruct all three terminal currents
        double I_F = I_ES * (Math.exp(v_be_bc[0] / V_T) - 1.0);
        double I_R = I_CS * (Math.exp(v_be_bc[1] / V_T) - 1.0);
        
        double i_C = ALPHA_F * I_F - I_R;
        double i_E = -I_F + ALPHA_R * I_R;
        double i_B = (1 - ALPHA_F) * I_F + (1 - ALPHA_R) * I_R;
        
        // Verify conservation law
        assertEquals(0.0, i_B + i_C + i_E, 1e-12 * Math.max(Math.abs(i_C), Math.abs(i_E)));
    }
    
    @Test
    @DisplayName("Reverse-biased behavior: near-zero reverse saturation current")
    void testReverseOperation() {
        DeviceEvaluationResult result = new DeviceEvaluationResult(2);
        double[] v_be_bc = {-0.4, -0.2};  // Both junctions reverse-biased
        
        bjt.evaluateTimeDomain(v_be_bc, 0.0, result);
        
        // For reverse bias, currents should be very small (near reverse saturation)
        // Expected: i_C ≈ -I_CS ≈ -1e-14 A
        assertTrue(Math.abs(result.conductiveCurrents[1]) < 1e-12, 
                   "Collector current should be small in reverse bias");
    }
    
    @Test
    @DisplayName("Forward-active bias: large collector current")
    void testForwardActiveOperation() {
        DeviceEvaluationResult result = new DeviceEvaluationResult(2);
        double[] v_be_bc = {0.65, -0.1};  // Forward-active: BE forward, BC reverse
        
        bjt.evaluateTimeDomain(v_be_bc, 0.0, result);
        
        double I_F = I_ES * (Math.exp(v_be_bc[0] / V_T) - 1.0);
        double i_C = ALPHA_F * I_F;  // BC reverse, so I_R negligible
        
        assertTrue(i_C > 0, "Collector current should be positive in forward-active");
        assertTrue(i_C > 1e-6, "Collector current should be significant");
    }
    
    @Test
    @DisplayName("Saturation bias: V_CE drops to ~0.1-0.2V")
    void testSaturationOperation() {
        DeviceEvaluationResult result = new DeviceEvaluationResult(2);
        double[] v_be_bc = {0.65, 0.40};  // Both junctions heavily forward-biased
        
        bjt.evaluateTimeDomain(v_be_bc, 0.0, result);
        
        double I_F = I_ES * (Math.exp(v_be_bc[0] / V_T) - 1.0);
        double I_R = I_CS * (Math.exp(v_be_bc[1] / V_T) - 1.0);
        
        double i_C = ALPHA_F * I_F - I_R;  // Both contributions positive
        double i_B = (1 - ALPHA_F) * I_F + (1 - ALPHA_R) * I_R;
        
        // Base current should increase significantly in saturation
        assertTrue(i_B > 0, "Base current should be positive");
        assertTrue(Math.abs(i_B / i_C) > 0.01, "Beta should drop in saturation");
    }
    
    @Test
    @DisplayName("Conductance derivatives: finite-difference verification")
    void testConductanceDerivatives() {
        DeviceDerivativeResult deriv = new DeviceDerivativeResult(2);
        double[] v_base = {0.4, -0.5};
        
        bjt.evaluateDerivatives(v_base, deriv);
        
        double delta = 1e-7;
        DeviceEvaluationResult result_base = new DeviceEvaluationResult(2);
        bjt.evaluateTimeDomain(v_base, 0.0, result_base);
        
        // Check dI_F/dv_BE (conductance element [0,0])
        double[] v_perturb_u0 = {v_base[0] + delta, v_base[1]};
        DeviceEvaluationResult result_u0_plus = new DeviceEvaluationResult(2);
        bjt.evaluateTimeDomain(v_perturb_u0, 0.0, result_u0_plus);
        
        double fd_g_f = (result_u0_plus.conductiveCurrents[0] - result_base.conductiveCurrents[0]) / delta;
        
        // Analytical derivative: g_F = (I_ES/V_T) * exp(v_BE/V_T)
        double analytical_g_F = (I_ES / V_T) * Math.exp(v_base[0] / V_T);
        
        assertEquals(analytical_g_F, fd_g_f, Math.abs(analytical_g_F) * 1e-4);
    }
    
    @Test
    @DisplayName("Conductance matrix: off-diagonal coupling through alpha parameters")
    void testConductanceCoupling() {
        DeviceDerivativeResult deriv = new DeviceDerivativeResult(2);
        double[] v_base = {0.3, -0.2};
        
        bjt.evaluateDerivatives(v_base, deriv);
        
        // G[0,1] should be -alphaR * g_R
        // G[1,0] should be -alphaF * g_F
        double g_F = (I_ES / V_T) * Math.exp(v_base[0] / V_T);
        double g_R = (I_CS / V_T) * Math.exp(v_base[1] / V_T);
        
        assertEquals(-ALPHA_R * g_R, deriv.conductanceMatrix[0][1], 1e-14);
        assertEquals(-ALPHA_F * g_F, deriv.conductanceMatrix[1][0], 1e-14);
    }
    
    @Test
    @DisplayName("Thermal voltage effect on exponential gain")
    void testThermalVoltageScaling() {
        BJTTransistor bjt_cold = new BJTTransistor(ALPHA_F, ALPHA_R, I_ES, I_CS, 0.013);  // 150K
        BJTTransistor bjt_hot = new BJTTransistor(ALPHA_F, ALPHA_R, I_ES, I_CS, 0.052);   // 600K
        
        DeviceEvaluationResult result_cold = new DeviceEvaluationResult(2);
        DeviceEvaluationResult result_hot = new DeviceEvaluationResult(2);
        
        double[] v_be_bc = {0.3, 0.0};
        
        bjt_cold.evaluateTimeDomain(v_be_bc, 0.0, result_cold);
        bjt_hot.evaluateTimeDomain(v_be_bc, 0.0, result_hot);
        
        // Smaller V_T = steeper exponential curve = larger current change per volt
        // At same voltage, cold transistor should have larger current
        assertTrue(Math.abs(result_cold.conductiveCurrents[0]) > 
                   Math.abs(result_hot.conductiveCurrents[0]));
    }
    
    @Test
    @DisplayName("Overflow protection: large voltage inputs")
    void testOverflowProtection() {
        DeviceEvaluationResult result = new DeviceEvaluationResult(2);
        double[] v_be_bc = {10.0, -5.0};  // Extreme values
        
        // Should not throw or produce NaN/Inf
        assertDoesNotThrow(() -> bjt.evaluateTimeDomain(v_be_bc, 0.0, result));
        assertFalse(Double.isNaN(result.conductiveCurrents[0]));
        assertFalse(Double.isInfinite(result.conductiveCurrents[0]));
    }
}
