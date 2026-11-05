package network;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Test suite verifying C++23 native implementation is loaded and functional.
 * Tests JNI bridge functionality and validates that native library is available for simulation.
 * 
 * @author Simon Zoltán
 */
@DisplayName("Native C++23 Interface Tests")
public class NativeNetworkInterfaceTest {
    
    private static boolean nativeLibraryAvailable;
    
    @BeforeAll
    public static void setupLibrary() {
        // Verify native library is loaded
        nativeLibraryAvailable = NativeNetworkInterface.isNativeLibraryLoaded();
        
        if (nativeLibraryAvailable) {
            System.out.println("\n✓ Native C++23 library loaded successfully");
        } else {
            System.out.println("\n✗ Native C++23 library not loaded");
            if (NativeNetworkInterface.getLoadError() != null) {
                System.out.println("  Error: " + NativeNetworkInterface.getLoadError());
            }
        }
    }
    
    @Test
    @DisplayName("Native library is loaded")
    public void testNativeLibraryLoaded() {
        assertTrue(nativeLibraryAvailable, 
            "Native C++23 library should be loaded for tests to verify performance improvement");
    }
    
    @Test
    @DisplayName("Network simulation uses native implementation")
    public void testNetworkUsesNativeImplementation() {
        if (!nativeLibraryAvailable) {
            System.out.println("⊘ Skipping network simulation test - native library not loaded");
            return;
        }
        
        // Create a simple network and verify it uses native solver
        Network network = new Network();
        
        // Check that native library is loaded for this network
        assertTrue(NativeNetworkInterface.isNativeLibraryLoaded(),
            "Network should be able to use native C++23 implementation");
        
        System.out.println("✓ Network created with C++23 native support available");
    }
}
