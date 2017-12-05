package com.github.vipeout23.mandelbroetchen.main;

import java.awt.image.BufferedImage;

import org.omg.CORBA.IMP_LIMIT;

import com.github.vipeout23.mandelbroetchen.main.Mandelfunctions.ComplexNumber;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Gui extends Application{
	
	private static final int SCALAR = 200;
	private static final int HEIGHT = 4*SCALAR, WIDTH = 7*SCALAR;
	private static final int ITERATIONS = 1000;
	private static final double ZOOM_RATIO = 0.1;
	
	//ViewBox (Edge 0 and Edge 2)
	private static ComplexNumber viewBoxEdge00 = new ComplexNumber(-2.5, 1.0);
	private static ComplexNumber viewBoxEdge20 = new ComplexNumber(1.0, -1.0);
	private static ComplexNumber viewBoxEdge0 = viewBoxEdge00;
	private static ComplexNumber viewBoxEdge2 = viewBoxEdge20;
	
	//ViewBox center
	private static ComplexNumber viewBoxCenter;
	
	private static double zoom = 1.0;
	private static boolean absolute = false;
	
	private static Stage primaryStage;
	private static ImageView imgView;
	
	public static Image generateImage(int width, int height, int iterations,
			ComplexNumber viewBoxEdge0, ComplexNumber viewBoxEdge2, boolean absoluteMode) {
		BufferedImage bimg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		int[] histogram = new int[iterations+1];
		int[][] iterationMap = new int[HEIGHT][WIDTH];
		
		for(int y = 0; y < height; y++) {			
			for(int x = 0; x < width; x++) {
				int it = Mandelfunctions.calculateIterationsAt(x, y, WIDTH, HEIGHT, iterations, viewBoxEdge0, viewBoxEdge2);
				histogram[it]++;
				iterationMap[y][x] = it;
			}
		}
		
		for(int y = 0; y < height; y++) {			
			for(int x = 0; x < width; x++) {
				double hue = 0.0;
				for(int i = 0; i < iterationMap[y][x]; i++) {
					if(!absoluteMode) {
						hue += (double)histogram[i] / (WIDTH*HEIGHT);						
					}else {
						hue = (double)iterationMap[y][x] / iterations;						
					}
				}
				bimg.setRGB(x, y, Mandelfunctions.getColorFromDivergeValue((double)hue));
			}
		}
		
		return SwingFXUtils.toFXImage(bimg, null);
	}
	
	private static void updateTitle() {
		primaryStage.setTitle(String.format("Location: %s  |  Zoom: %.15f  |  Iterations: %d  |  RenderMode: %s",
				viewBoxCenter, zoom, ITERATIONS, (absolute) ? "Absolute" : "Histogram"));
	}
	
	private static void jumpTo(ComplexNumber viewBoxEdge0, ComplexNumber viewBoxEdge2) {
		imgView.setImage(generateImage(WIDTH, HEIGHT, ITERATIONS, viewBoxEdge0, viewBoxEdge2, absolute));
		Gui.viewBoxEdge0 = viewBoxEdge0;
		Gui.viewBoxEdge2 = viewBoxEdge2;
		updateTitle();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Gui.primaryStage = primaryStage;
		
		BorderPane pane = new BorderPane();
		ImageView imgView = new ImageView();
		pane.setCenter(imgView);
		
		Gui.imgView = imgView;
		
		//Build button bar
		ButtonBar bar = new ButtonBar();
		Button zoomReset = new Button("Reset Zoom");
		Button exprot = new Button("Export");
		Button load = new Button("Import");
		Button switchRenderMode = new Button("Switch RenderMode");
		
		bar.getButtons().addAll(zoomReset, switchRenderMode, load, exprot);
		pane.setTop(bar);
		
		zoomReset.setOnAction(e -> {
			jumpTo(viewBoxEdge00, viewBoxEdge20);
		});
		switchRenderMode.setOnAction(e -> {
			absolute = !absolute;
			jumpTo(viewBoxEdge0, viewBoxEdge2);
		});
		
		imgView.setImage(generateImage(WIDTH, HEIGHT, ITERATIONS, viewBoxEdge0, viewBoxEdge2, true));
		updateTitle();
		
		imgView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				double zoomRatio = 0.0;
				if(event.getButton() == MouseButton.PRIMARY) {
					zoomRatio = ZOOM_RATIO;
				}else if(event.getButton() == MouseButton.SECONDARY) {
					zoomRatio = 1.0/ZOOM_RATIO;
				}
				
				//Calculate relative mouse location
				double xRatio = event.getX() / WIDTH;
				double yRatio = event.getY() / HEIGHT;
				
				//Distances of the current viewbox
				double realDist = Math.abs(viewBoxEdge2.realPart-viewBoxEdge0.realPart);
				double imagDist = Math.abs(viewBoxEdge2.imaginaryPart-viewBoxEdge0.imaginaryPart);
				
				//Calculate center of the new viewbox
				ComplexNumber center = new ComplexNumber(
						viewBoxEdge0.realPart + (realDist*xRatio),
						viewBoxEdge0.imaginaryPart - (imagDist*yRatio)
				);
				
				//Calculate the new zoomed in viewbox
				ComplexNumber vb0 = new ComplexNumber(center.realPart - zoomRatio*realDist*0.5,
						center.imaginaryPart + zoomRatio*imagDist*0.5);
				ComplexNumber vb2 = new ComplexNumber(center.realPart + zoomRatio*realDist*0.5,
						center.imaginaryPart - zoomRatio*imagDist*0.5);
				
				//Calculate zoom factor
				zoom = realDist*zoomRatio / Math.abs(viewBoxEdge00.realPart - viewBoxEdge20.realPart);
				
				viewBoxEdge0 = vb0;
				viewBoxEdge2 = vb2;
				
				jumpTo(vb0, vb2);
				
				viewBoxCenter = center;
				
				updateTitle();
			}
		});
		
		Parent root = pane;
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setWidth(WIDTH);
		primaryStage.setHeight(HEIGHT);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(Gui.class, args);
	}
}
