const { pulsar } = require('./pulsar');
const { forEngineMessage } = require('./avro');

(async () => {
  const TOPIC = "tasks-engine"

  // Create a consumer
  const consumer = await pulsar.subscribe({
    topic: `persistent://public/default/${TOPIC}`,
    subscription: TOPIC,
    subscriptionType: 'Shared',
    ackTimeoutMs: 10000,
  });

  // Receive messages
  for (let i = 0; i < 1000; i += 1) {
    const msg = await consumer.receive();
    console.log(forEngineMessage.fromBuffer(msg.getData()))
    consumer.acknowledge(msg);
  }

  await consumer.close();
  await pulsar.close();
})();
