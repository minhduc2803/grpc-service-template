package vn.toanhda.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class DisruptorCreator {
  private static Disruptor disruptor;
  private static RingBuffer ringBuffer;

  public static <T> Disruptor getInstance(
      EventFactory<StorageEvent> factory, DisruptorConfig disruptorConfig) {
    if (disruptor == null) {
      disruptor = newInstance(factory, disruptorConfig);
    }
    return disruptor;
  }

  public static RingBuffer getRingBuffer() {
    return ringBuffer;
  }

  public static <T> Disruptor newInstance(
      EventFactory<StorageEvent> factory, DisruptorConfig disruptorConfig) {
    ProducerType producerType =
        disruptorConfig.getProducerType().equals("MULTI")
            ? ProducerType.MULTI
            : ProducerType.SINGLE;

    Disruptor disruptor =
        new Disruptor(
            factory,
            disruptorConfig.getBufferSize(),
            DaemonThreadFactory.INSTANCE,
            producerType,
            new BusySpinWaitStrategy());

    disruptor.handleEventsWithWorkerPool(getWorkersPool(disruptorConfig.getNumWorkers()));
    ringBuffer = disruptor.start();

    return disruptor;
  }

  static WorkHandler<StorageEvent>[] getWorkersPool(int numWorkers) {
    WorkHandler<StorageEvent>[] workHandlers = new WorkHandler[numWorkers];
    for (int i = 0; i < numWorkers; i++) {
      workHandlers[i] = new StorageConsumer();
    }
    return workHandlers;
  }
}
