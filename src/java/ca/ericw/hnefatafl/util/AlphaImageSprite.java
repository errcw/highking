package ca.ericw.hnefatafl.util;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;

import com.threerings.media.sprite.ImageSprite;

/**
 * A sprite that provides control over the alpha value it uses to render itself.
 * The alpha may be either set directly or animated.
 * 
 * An alpha sprite is very similar in nature to the FadeableImageSprite but
 * offers a slightly different interface that provides the functionality this
 * game requires. This implementation borrows code from the Three Rings version.
 * 
 * @author Three Rings Design Inc.
 */
public class AlphaImageSprite extends ImageSprite {
    
    /**
     * Fades this sprite from its current alpha value to another alpha value
     * over the given duration.
     * @param endAlpha final alpha value for this sprite
     * @param duration time to animate the fade, in milliseconds
     */
    public void animateFade(float endAlpha, long duration) {
        animateFade(getAlpha(), endAlpha, duration);
    }
    
    /**
     * Fades this sprite between two alpha values over the given duration.
     * @param startAlpha initial alpha value for this sprite
     * @param endAlpha final alpha value for this sprite
     * @param duration time to animate the fade, in milliseconds
     */
    public void animateFade(float startAlpha, float endAlpha, long duration) {
        _initialAlpha = startAlpha;
        _finalAlpha = endAlpha;
        _fadeDuration = duration;
        setAlpha(_initialAlpha);
    }
    
    /**
     * Cancels any fade animation currently in progress. The alpha value will be
     * reset to its initial value before the animation was started.
     */
    public void cancelFade() {
        _fadeDuration = -1;
        _fadeStamp = 0;
        setAlpha(_initialAlpha);
    }
    
    @Override // from AbstractMedia
    public void tick(long timestamp) {
        super.tick(timestamp);
        
        if (_fadeDuration != -1) {
            if (_fadeStamp == 0) {
                _fadeStamp = timestamp;
            }
            
            float percent = (float)(timestamp - _fadeStamp) / _fadeDuration;
            float delta = _finalAlpha - _initialAlpha;
            float alpha = _initialAlpha + percent * delta;
            
            if (alpha == _finalAlpha) { //FIXME this comparison is no good
                _fadeDuration = -1;
                _fadeStamp = 0;
                setAlpha(_finalAlpha);
            } else {
                setAlpha(alpha);
            }
        }
    }

    @Override // from AbstractMedia
    public void paint (Graphics2D gfx) {
        if (getAlpha() == 0f) {
            return;
        } else if (getAlpha() < 1.0f) {
            Composite oldComp = gfx.getComposite();
            gfx.setComposite(_alphaComposite);
            super.paint(gfx);
            gfx.setComposite(oldComp);
        } else {
            super.paint(gfx);
        }
    }

    
    /**
     * Sets the alpha value used to render this sprite.
     */
    public void setAlpha (float alpha) {
        if (alpha < 0.0f) {
            alpha = 0.0f;
        } else if (alpha > 1.0f) {
            alpha = 1.0f;
        }
        
        if (alpha != _alphaComposite.getAlpha()) {
            _alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            invalidate();
        }
    }

    /**
     * Returns the alpha value used to render this sprite.
     */
    public float getAlpha () {
        return _alphaComposite.getAlpha();
    }
    
    /** The alpha composite. */
    protected AlphaComposite _alphaComposite =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);

    /** Duration, in milliseconds, of the fade animation. */
    protected long _fadeDuration = -1;
    
    /** Timestamp of the start of the fade animation. */
    protected long _fadeStamp = 0;
    
    /** Initial alpha value for the fade animation. */
    protected float _initialAlpha;
    
    /** Result alpha value for the fade animation. */
    protected float _finalAlpha;
}
