package com.tyron.layoutpreview.inflate;

import android.content.Context;
import android.util.Log;
import android.view.InflateException;

import com.flipkart.android.proteus.Proteus;
import com.flipkart.android.proteus.ProteusBuilder;
import com.flipkart.android.proteus.ProteusContext;
import com.flipkart.android.proteus.ProteusLayoutInflater;
import com.flipkart.android.proteus.ProteusView;
import com.flipkart.android.proteus.SimpleIdGenerator;
import com.flipkart.android.proteus.SimpleLayoutInflater;
import com.flipkart.android.proteus.value.Layout;
import com.flipkart.android.proteus.value.ObjectValue;
import com.flipkart.android.proteus.value.Value;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.tyron.layoutpreview.convert.XmlToJsonConverter;
import com.tyron.layoutpreview.convert.adapter.ProteusTypeAdapterFactory;
import com.tyron.layoutpreview.model.Attribute;
import com.tyron.layoutpreview.model.CustomView;
import com.tyron.layoutpreview.parser.CustomViewParser;
import com.tyron.layoutpreview.view.UnknownView;

import java.io.StringReader;
import java.util.Collections;

public class PreviewLayoutInflater {

    private final Context mBaseContext;
    private final Proteus mProteus;
    private final ProteusContext mContext;
    private final CustomProteusInflater mInflater;

    private final ProteusLayoutInflater.Callback mCallback = new ProteusLayoutInflater.Callback() {
        @Override
        public ProteusView onUnknownViewType(ProteusContext context, String type, Layout layout, ObjectValue data, int index) {
            return new UnknownView(context, type);
        }

        @Override
        public void onEvent(String event, Value value, ProteusView view) {

        }
    };

    public PreviewLayoutInflater(Context base) {
        mBaseContext = base;
        mProteus = new ProteusBuilder()
                .register(new CustomViewParser(getTestView()))
                .build();
        mContext = mProteus.createContextBuilder(base)
                .setCallback(mCallback)
                .build();

        mInflater = new CustomProteusInflater(mContext, new SimpleIdGenerator());

        ProteusTypeAdapterFactory.PROTEUS_INSTANCE_HOLDER.setProteus(mProteus);
    }

    private CustomView getTestView() {
        CustomView view = new CustomView();
        view.setType("androidx.cardview.widget.CardView");
        view.setParentType("FrameLayout");
        Attribute attribute = new Attribute();
        attribute.setMethodName("setCardBackgroundColor");
        attribute.setParameters(new String[]{int.class.getName()});
        attribute.setXmlParameterOffset(0);
        attribute.setXmlName("app:cardBackgroundColor");
        view.setAttributes(Collections.singletonList(attribute));
        return view;
    }

    public ProteusView inflate(String xml) throws InflateException {
        try {
            JsonObject object = new XmlToJsonConverter()
                    .convert(xml);
            return inflate(object);
        } catch (Exception e) {
            throw new InflateException("Unable to inflate layout: " + e.getMessage());
        }
    }

    /**
     * Convenience method to inflate a layout using a {@link JsonObject}
     * @param object The json object to inflate
     * @return The inflated view
     */
    public ProteusView inflate(JsonObject object) {
        try {
            Value value = new ProteusTypeAdapterFactory(mContext)
                    .VALUE_TYPE_ADAPTER.read(new JsonReader(new StringReader(object.toString())));
            return inflate(value.getAsLayout());
        } catch (Exception e) {
            throw new InflateException("Unable to inflate layout: " + Log.getStackTraceString(e));
        }
    }

    public ProteusView inflate(Layout layout) {
        return mInflater.inflate(layout, new ObjectValue());
    }
}
