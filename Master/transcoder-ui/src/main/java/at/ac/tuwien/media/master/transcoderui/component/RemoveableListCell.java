package at.ac.tuwien.media.master.transcoderui.component;

import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import at.ac.tuwien.media.master.commons.IOnRemoveElementListener;

public class RemoveableListCell extends ListCell<String> {
    private final HBox f_aHBox = new HBox();
    private final Label f_aLabel = new Label();

    public RemoveableListCell(@Nonnull final IOnRemoveElementListener aRemoveElementListener) {
	final Pane aPane = new Pane();
	HBox.setHgrow(aPane, Priority.ALWAYS);

	final Hyperlink aHyperlink = new Hyperlink("✗");
	aHyperlink.setOnAction(event -> {
	    aRemoveElementListener.onRemoveElement(getIndex());
	});

	f_aHBox.getChildren().addAll(f_aLabel, aPane, aHyperlink);
    }

    @Override
    protected void updateItem(final String sText, final boolean bIsEmpty) {
	super.updateItem(sText, bIsEmpty);
	setText(null);

	if (bIsEmpty)
	    setGraphic(null);
	else {
	    f_aLabel.setText(StringUtils.isNotEmpty(sText) ? sText : "-");
	    setGraphic(f_aHBox);
	}
    }

}