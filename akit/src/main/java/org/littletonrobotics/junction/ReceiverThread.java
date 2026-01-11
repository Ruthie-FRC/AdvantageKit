// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

class ReceiverThread extends Thread {
  private final BlockingQueue<LogTable> queue;
  private List<LogDataReceiver> dataReceivers = new ArrayList<>();

  ReceiverThread(BlockingQueue<LogTable> queue) {
    super("AdvantageKit_LogReceiver");
    this.setDaemon(true);
    this.queue = queue;
  }

  void addDataReceiver(LogDataReceiver dataReceiver) {
    dataReceivers.add(dataReceiver);
  }

  public void run() {
    // Start data receivers
    for (LogDataReceiver receiver : dataReceivers) {
      receiver.start();
    }

    try {
      while (true) {
        LogTable entry = queue.take(); // Wait for data

        // Send data to receivers
        for (LogDataReceiver receiver : dataReceivers) {
          receiver.putTable(entry);
        }
      }
    } catch (InterruptedException exception) {
      // Empty queue
      while (!queue.isEmpty()) {
        LogTable entry = queue.poll();
        for (LogDataReceiver receiver : dataReceivers) {
          try {
            receiver.putTable(entry);
          } catch (InterruptedException e) {
            // Interrupted while emptying queue, continue to next receiver
          }
        }
      }

      // End all data receivers
      for (LogDataReceiver receiver : dataReceivers) {
        receiver.end();
      }
    }
  }
}
