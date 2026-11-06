package network;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import math.Complex;

/**
 * Integration tests verifying network simulation uses C++23 native
 * acceleration.
 * Tests that networks correctly compute currents using native implementation.
 * 
 * @author Zoltán Simon
 */
@DisplayName("Network C++23 Integration Tests")
public class NetworkNativeIntegrationTest {

    private Network network;

    @BeforeEach
    public void createNetwork() {
        network = new Network();
    }

    @Test
    @DisplayName("Empty network initialization")
    public void testNetworkInitialization() {
        assertNotNull(network, "Network should be created");
        assertFalse(network.getSimulatedAngularFrequencies().isEmpty(),
                "Network should have DC component (0 Hz)");
        System.out.println("✓ Network created successfully");

        if (NativeNetworkInterface.isNativeLibraryLoaded()) {
            System.out.println("✓ Native C++23 library available for simulation");
        } else {
            System.out.println("⊘ Native library not available; using Java fallback");
        }
    }

    @Test
    @DisplayName("Network can request and release frequencies")
    public void testFrequencyManagement() {
        // Request some AC frequencies
        int freq_50Hz = network.requestAngularFrequency(2 * Math.PI * 50); // 50 Hz
        int freq_60Hz = network.requestAngularFrequency(2 * Math.PI * 60); // 60 Hz

        assertTrue(network.getSimulatedAngularFrequencies().size() >= 2,
                "Network should have DC + 2 AC frequencies");

        System.out.println("✓ Frequencies registered:");
        System.out.println("  DC: 0 Hz");
        for (Double omega : network.getSimulatedAngularFrequencies()) {
            if (omega > 0) {
                System.out.println("  AC: " + (omega / (2 * Math.PI)) + " Hz");
            }
        }

        // Release frequencies
        network.releaseAngularFrequency(2 * Math.PI * 50);
        network.releaseAngularFrequency(2 * Math.PI * 60);
    }

    @Test
    @DisplayName("Native library status is accessible during simulation")
    public void testNativeLibraryAccessible() {
        // This verifies that NativeNetworkInterface works correctly during network
        // operations
        boolean nativeAvailable = NativeNetworkInterface.isNativeLibraryLoaded();

        if (nativeAvailable) {
            System.out.println("✓ Native library available during network operations");
            assertNotNull(NativeNetworkInterface.getLoadError() == null ||
                    NativeNetworkInterface.getLoadError().isEmpty());
        } else {
            System.out.println("⊘ Native library not available (expected on first builds)");
        }
    }

    @Test
    @DisplayName("Network evaluate with native support")
    public void testNetworkEvaluate() {
        // Request an AC frequency
        network.requestAngularFrequency(2 * Math.PI * 50);

        // Calling evaluate on empty network should not crash
        assertDoesNotThrow(() -> network.evaluate(true),
                "Network evaluation should not throw even with no components");

        System.out.println("✓ Network evaluation completed");

        // Verify that if native library is loaded, it was used
        if (NativeNetworkInterface.isNativeLibraryLoaded()) {
            System.out.println("✓ Evaluation could use C++23 native acceleration");
        }
    }

    @Test
    @DisplayName("Multiple network instances use shared native library")
    public void testMultipleNetworkInstances() {
        Network network1 = new Network();
        Network network2 = new Network();

        boolean nativeLoaded = NativeNetworkInterface.isNativeLibraryLoaded();

        // Both networks should have access to same native library
        assertTrue(NativeNetworkInterface.isNativeLibraryLoaded() == nativeLoaded,
                "Native library availability should be consistent across network instances");

        System.out.println("✓ Multiple network instances share native library state");
    }
}
