// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.rlog;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.ServerSocket;
import org.junit.jupiter.api.Test;

class RLOGServerTest {

  /** Test that the server can be started and stopped without leaving the port busy */
  @Test
  void testServerCloseReleasesPort() throws InterruptedException {
    int testPort = 5801; // Use a different port to avoid conflicts

    // Create and start first server
    RLOGServer server1 = new RLOGServer(testPort);
    server1.start();
    Thread.sleep(100); // Give server time to bind

    // Close the server
    server1.close();
    Thread.sleep(100); // Give server time to release port

    // Verify port is released by creating a new server on same port
    RLOGServer server2 = new RLOGServer(testPort);
    server2.start();
    Thread.sleep(100); // Give server time to bind

    // Clean up
    server2.close();
    Thread.sleep(100);
  }

  /** Test that try-with-resources properly closes the server */
  @Test
  void testTryWithResources() throws InterruptedException {
    int testPort = 5802;

    // Use try-with-resources
    try (RLOGServer server = new RLOGServer(testPort)) {
      server.start();
      Thread.sleep(100);
    } // Server should auto-close here
    Thread.sleep(100);

    // Verify port is released
    try (ServerSocket testSocket = new ServerSocket(testPort)) {
      // Port is available
      assertTrue(true);
    } catch (IOException e) {
      fail("Port should be available after try-with-resources: " + e.getMessage());
    }
  }

  /** Test that end() method still works (backward compatibility) */
  @Test
  void testEndMethodBackwardCompatibility() throws InterruptedException {
    int testPort = 5803;

    RLOGServer server1 = new RLOGServer(testPort);
    server1.start();
    Thread.sleep(100);

    // Use old end() method
    server1.end();
    Thread.sleep(100);

    // Verify port is released
    RLOGServer server2 = new RLOGServer(testPort);
    server2.start();
    Thread.sleep(100);

    server2.close();
    Thread.sleep(100);
  }

  /** Test concurrent access to close method */
  @Test
  void testConcurrentClose() throws InterruptedException {
    int testPort = 5804;

    RLOGServer server = new RLOGServer(testPort);
    server.start();
    Thread.sleep(100);

    // Try closing from multiple threads simultaneously
    Thread t1 = new Thread(() -> server.close());
    Thread t2 = new Thread(() -> server.close());
    Thread t3 = new Thread(() -> server.end());

    t1.start();
    t2.start();
    t3.start();

    t1.join();
    t2.join();
    t3.join();

    Thread.sleep(100);

    // Verify port is released
    try (ServerSocket testSocket = new ServerSocket(testPort)) {
      assertTrue(true);
    } catch (IOException e) {
      fail("Port should be available after concurrent close: " + e.getMessage());
    }
  }
}
