import { NextResponse } from 'next/server';
import { generateClinicalAssessment, evaluateEmergency } from '@/lib/triage';

export async function POST(req: Request) {
  try {
    const body = await req.json();
    const { imageType, bodyArea, injuryMechanism, symptoms, painLevel, description, hasImage } = body;

    if (!bodyArea || !injuryMechanism) {
      return NextResponse.json(
        { error: 'bodyArea and injuryMechanism are required fields.' },
        { status: 400 }
      );
    }

    const assessment = generateClinicalAssessment({
      imageType: imageType || 'EXTERNAL_PHOTO',
      bodyArea,
      injuryMechanism,
      symptoms: Array.isArray(symptoms) ? symptoms : [],
      painLevel: typeof painLevel === 'number' ? painLevel : 3,
      description: description || '',
      hasImage: Boolean(hasImage)
    });

    return NextResponse.json({ success: true, assessment });
  } catch (err: any) {
    return NextResponse.json(
      { error: err.message || 'Internal server error processing assessment.' },
      { status: 500 }
    );
  }
}
