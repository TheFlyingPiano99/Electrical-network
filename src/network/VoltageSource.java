package network;

public class VoltageSource extends Component {
	
	public VoltageSource() {
	}
	
	
	public VoltageSource(float u) {
		create();
	}
	
	public VoltageSource(float r, float i, float u) {
		super(r, i ,u);
	}
	
}
