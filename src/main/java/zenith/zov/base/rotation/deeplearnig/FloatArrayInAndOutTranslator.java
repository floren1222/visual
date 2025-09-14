package zenith.zov.base.rotation.deeplearnig;

import ai.djl.ndarray.NDList;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

public class FloatArrayInAndOutTranslator implements Translator<float[], float[]> {

    @Override
    public NDList processInput(TranslatorContext ctx, float[] input) {
        return new NDList(ctx.getNDManager().create(input));
    }

    @Override
    public float[] processOutput(TranslatorContext ctx, NDList list) {
        return list.get(0).toFloatArray();
    }
}
