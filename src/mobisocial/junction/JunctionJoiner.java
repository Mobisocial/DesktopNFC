package mobisocial.junction;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import edu.stanford.junction.JunctionException;
import edu.stanford.junction.JunctionMaker;
import edu.stanford.junction.api.activity.JunctionActor;
import edu.stanford.junction.api.messaging.MessageHeader;

public class JunctionJoiner {
	private final JunctionMaster mMaster;
	
	public static void main(String[] args) throws JunctionException {
		URI[] uris = new URI[] {
			URI.create("junction://sb.openjunction.org/a"),
			URI.create("junction://sb.openjunction.org/b"),
			URI.create("junction://sb.openjunction.org/c"),
		};

		JunctionJoiner joiner = new JunctionJoiner();
		joiner.join(uris);
		synchronized (uris) {
			try {
				uris.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
	}
	
	public synchronized void join(URI... sessions) throws JunctionException {
		for (URI session: sessions) {
			JunctionMaker.bind(session, new JunctionSlave(mMaster));
		}
	}

	public synchronized void split() {
		for (JunctionActor slave : mMaster.mSlaves) {
			slave.leave();
		}
		mMaster.mSlaves.clear();
	}

	public JunctionJoiner() {
		mMaster = new JunctionMaster();
	}

	static class JunctionMaster {
		List<JunctionActor> mSlaves = new ArrayList<JunctionActor>();

		public void addSlave(JunctionActor slave) {
			mSlaves.add(slave);
		}
		
		public void relay(JunctionActor snitch, MessageHeader header, JSONObject message) {
			for (JunctionActor actor : mSlaves) {
				if (actor != snitch) {
					// TODO: Send messages "on behalf of" others.
					header.from = actor.getActorID();
					actor.sendMessageToSession(message);
				}
			}
		}
	}
	
	static class JunctionSlave extends JunctionActor {
		public JunctionMaster mMaster;

		@Override
		public void onMessageReceived(MessageHeader header, JSONObject message) {
		    if (!header.from.equals(getActorID())) {
                mMaster.relay(this, header, message);
            }
		}
		
		public JunctionSlave(JunctionMaster master) {
			mMaster = master;
			master.addSlave(this);
		}
	}
}