import { NextResponse } from 'next/server';

export async function POST(req: Request) {
  try {
    const body = await req.json();
    const { injuryRecordId, painLevel, swellingStatus, mobilityScore, progressNotes, reportedRecoveryStatus } = body;

    if (!injuryRecordId) {
      return NextResponse.json(
        { error: 'injuryRecordId is required.' },
        { status: 400 }
      );
    }

    const log = {
      id: 'log_' + Date.now(),
      injury_record_id: injuryRecordId,
      pain_level: painLevel,
      swelling_status: swellingStatus || 'Mild',
      mobility_score: mobilityScore || 3,
      progress_notes: progressNotes || '',
      reported_recovery_status: reportedRecoveryStatus || 'Healing well',
      created_at: new Date().toISOString()
    };

    return NextResponse.json({ success: true, log });
  } catch (err: any) {
    return NextResponse.json(
      { error: err.message || 'Internal server error recording recovery log.' },
      { status: 500 }
    );
  }
}
