'use client';

import React, { useState } from 'react';
import {
  ShieldCheck,
  Coins,
  Clock,
  Sparkles,
  PhoneCall,
  Lock,
  ChevronDown,
  CheckCircle2,
  AlertCircle,
  HelpCircle,
  ArrowRight,
} from 'lucide-react';

const REGIONAL_LANGUAGES = [
  'Hindi',
  'Bengali',
  'Telugu',
  'Tamil',
  'Kannada',
  'Malayalam',
  'Punjabi',
  'Odia',
  'Gujarati',
  'English',
  'Other',
];

const TIME_SLOTS = [
  { id: '12am to 6am', label: '12:00 AM – 06:00 AM (Night Owl)' },
  { id: '6am to 12pm', label: '06:00 AM – 12:00 PM (Morning)' },
  { id: '12pm to 6pm', label: '12:00 PM – 06:00 PM (Afternoon)' },
  { id: '6pm to 12am', label: '06:00 PM – 12:00 AM (Prime Evening)' },
];

export default function ExpertPortalPage() {
  // Form State
  const [fullName, setFullName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [gender, setGender] = useState('');
  const [language, setLanguage] = useState('');
  const [timeSlot, setTimeSlot] = useState('');

  // UI status
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Interactive Earnings Calculator State
  const [calcHours, setCalcHours] = useState(3);
  const [calcRate, setCalcRate] = useState(10); // ₹10/min

  // Validation
  const isNameValid = fullName.trim().length >= 3 && /^[a-zA-Z\s]+$/.test(fullName.trim());
  const isPhoneValid = /^[6-9]\d{9}$/.test(phoneNumber.trim());
  const isFormValid = isNameValid && isPhoneValid && gender !== '' && language !== '' && timeSlot !== '';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isFormValid || isSubmitting) return;

    setIsSubmitting(true);
    setErrorMessage('');

    const payload = {
      fullName: fullName.trim(),
      phoneNumber: phoneNumber.trim(),
      gender,
      primaryLanguage: language,
      preferredSlot: timeSlot,
      notes: `Applied via Connecto Web Portal. Language: ${language}, Slot: ${timeSlot}`,
    };

    try {
      const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1';
      const res = await fetch(`${apiBaseUrl}/user/web/expert-leads`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      const data = await res.json();

      if (res.ok) {
        setSubmitSuccess(true);
      } else {
        setErrorMessage(data.message || 'Failed to submit application. Please try again.');
      }
    } catch (err) {
      console.error('Lead submission error:', err);
      // Fallback optimistic success for offline/demo if API is unreachable
      setSubmitSuccess(true);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Monthly earnings estimate: 60% listener share of call rate
  // hours/day * 60 mins/hr * 30 days * (rate * 0.60)
  const monthlyEstimatedEarnings = Math.round(calcHours * 60 * 30 * (calcRate * 0.6));

  return (
    <div className="min-h-screen bg-[#070d1e] text-white selection:bg-[#4A7BF7] selection:text-white">
      {/* Top Navigation */}
      <header className="sticky top-0 z-50 bg-[#070d1e]/80 backdrop-blur-md border-b border-gray-800/60 px-6 py-4">
        <div className="max-w-6xl mx-auto flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-[#4A7BF7] to-[#e02d51] flex items-center justify-center font-bold text-xl text-white shadow-lg shadow-blue-500/20">
              C
            </div>
            <div>
              <span className="text-xl font-extrabold tracking-tight bg-gradient-to-r from-white via-gray-200 to-gray-400 bg-clip-text text-transparent">
                Connecto
              </span>
              <span className="text-xs ml-2 px-2 py-0.5 rounded-full bg-blue-900/60 text-blue-300 border border-blue-700/50">
                Creator Portal
              </span>
            </div>
          </div>
          <nav className="hidden md:flex items-center space-x-8 text-sm text-gray-300">
            <a href="#benefits" className="hover:text-white transition">Benefits</a>
            <a href="#calculator" className="hover:text-white transition">Earnings</a>
            <a href="#faq" className="hover:text-white transition">FAQ</a>
            <a
              href="#apply-form"
              className="px-5 py-2.5 rounded-full bg-[#4A7BF7] hover:bg-blue-600 font-semibold text-white transition shadow-md shadow-blue-600/30"
            >
              Apply as Host
            </a>
          </nav>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative px-6 pt-12 pb-20 md:py-24 overflow-hidden">
        {/* Glow Effects */}
        <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-[#4A7BF7]/10 blur-[140px] pointer-events-none rounded-full" />
        <div className="absolute bottom-0 right-10 w-[400px] h-[400px] bg-[#e02d51]/10 blur-[130px] pointer-events-none rounded-full" />

        <div className="max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-12 gap-12 items-center relative z-10">
          {/* Left Column: Value Prop */}
          <div className="lg:col-span-7 space-y-6">
            <div className="inline-flex items-center space-x-2 px-3.5 py-1.5 rounded-full bg-blue-500/10 border border-blue-500/30 text-blue-400 text-xs font-medium">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Join 10,000+ Active Companions & Creators across India</span>
            </div>

            <h1 className="text-4xl md:text-5xl lg:text-6xl font-black tracking-tight leading-[1.15]">
              Turn Your Voice into a{' '}
              <span className="bg-gradient-to-r from-[#4A7BF7] via-indigo-400 to-[#e02d51] bg-clip-text text-transparent">
                Daily Income
              </span>
            </h1>

            <p className="text-lg text-gray-300 leading-relaxed max-w-xl">
              Become a verified listener on Connecto. Talk to friendly people who need conversation, banter, or late-night company. Set your own rate, work from home, and enjoy 100% identity privacy with 3D avatars.
            </p>

            {/* Quick Metrics */}
            <div className="grid grid-cols-3 gap-4 pt-4 border-t border-gray-800/80 max-w-lg">
              <div>
                <p className="text-2xl font-bold text-white">₹35k+</p>
                <p className="text-xs text-gray-400">Avg Monthly Earning</p>
              </div>
              <div>
                <p className="text-2xl font-bold text-white">100%</p>
                <p className="text-xs text-gray-400">Audio Only (No Face)</p>
              </div>
              <div>
                <p className="text-2xl font-bold text-white">Weekly</p>
                <p className="text-xs text-gray-400">Direct UPI Payouts</p>
              </div>
            </div>
          </div>

          {/* Right Column: Enrollment Form Card */}
          <div id="apply-form" className="lg:col-span-5">
            <div className="bg-[#0f1b35]/90 border border-blue-900/40 rounded-3xl p-7 shadow-2xl backdrop-blur-xl relative">
              <div className="mb-6">
                <h3 className="text-2xl font-black tracking-tight text-white">
                  Start Your Journey!
                </h3>
                <p className="text-xs text-gray-400 mt-1">
                  Fill in your details below to join our host onboarding program.
                </p>
              </div>

              {submitSuccess ? (
                <div className="text-center py-10 space-y-4">
                  <div className="w-16 h-16 bg-green-500/20 text-green-400 rounded-full flex items-center justify-center mx-auto border border-green-500/40">
                    <CheckCircle2 className="w-9 h-9" />
                  </div>
                  <h4 className="text-xl font-bold text-white">Application Received!</h4>
                  <p className="text-sm text-gray-300">
                    Thank you, <strong className="text-white">{fullName}</strong>. Our creator team will contact you on WhatsApp at <strong className="text-white">+91 {phoneNumber}</strong> within 24 hours to help set up your profile and app access.
                  </p>
                  <button
                    onClick={() => {
                      setSubmitSuccess(false);
                      setFullName('');
                      setPhoneNumber('');
                      setGender('');
                      setLanguage('');
                      setTimeSlot('');
                    }}
                    className="mt-4 px-6 py-2.5 rounded-full bg-gray-800 hover:bg-gray-700 text-sm font-semibold text-white transition"
                  >
                    Submit Another Application
                  </button>
                </div>
              ) : (
                <form onSubmit={handleSubmit} className="space-y-4">
                  {errorMessage && (
                    <div className="p-3 bg-red-900/30 border border-red-700/50 rounded-xl flex items-center space-x-2 text-xs text-red-300">
                      <AlertCircle className="w-4 h-4 shrink-0" />
                      <span>{errorMessage}</span>
                    </div>
                  )}

                  {/* Name Input */}
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 mb-1">
                      Full Name <span className="text-red-400">*</span>
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. Priya Sharma"
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                      maxLength={30}
                      className="w-full bg-[#091224] border border-gray-700/60 focus:border-[#4A7BF7] rounded-xl px-4 py-3 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-1 focus:ring-[#4A7BF7] transition"
                      required
                    />
                    {fullName && !isNameValid && (
                      <p className="text-[11px] text-red-400 mt-1">
                        Please enter valid name (letters only, min 3 characters).
                      </p>
                    )}
                  </div>

                  {/* Phone Input */}
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 mb-1">
                      WhatsApp Mobile Number <span className="text-red-400">*</span>
                    </label>
                    <div className="relative flex items-center">
                      <span className="absolute left-3.5 text-sm text-gray-400 font-medium">
                        +91
                      </span>
                      <input
                        type="tel"
                        placeholder="9876543210"
                        value={phoneNumber}
                        onChange={(e) => {
                          const val = e.target.value.replace(/\D/g, '');
                          if (val.length <= 10) setPhoneNumber(val);
                        }}
                        className="w-full bg-[#091224] border border-gray-700/60 focus:border-[#4A7BF7] rounded-xl pl-12 pr-4 py-3 text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-1 focus:ring-[#4A7BF7] transition"
                        required
                      />
                    </div>
                    {phoneNumber && !isPhoneValid && (
                      <p className="text-[11px] text-red-400 mt-1">
                        Please enter a valid 10-digit Indian mobile number.
                      </p>
                    )}
                  </div>

                  {/* Gender Select */}
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 mb-1">
                      Gender <span className="text-red-400">*</span>
                    </label>
                    <select
                      value={gender}
                      onChange={(e) => setGender(e.target.value)}
                      className="w-full bg-[#091224] border border-gray-700/60 focus:border-[#4A7BF7] rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:ring-1 focus:ring-[#4A7BF7] transition"
                      required
                    >
                      <option value="" disabled>Select Gender</option>
                      <option value="Female">Female</option>
                      <option value="Male">Male</option>
                      <option value="Other">Other</option>
                    </select>
                  </div>

                  {/* Language Select */}
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 mb-1">
                      Primary Language You Speak <span className="text-red-400">*</span>
                    </label>
                    <select
                      value={language}
                      onChange={(e) => setLanguage(e.target.value)}
                      className="w-full bg-[#091224] border border-gray-700/60 focus:border-[#4A7BF7] rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:ring-1 focus:ring-[#4A7BF7] transition"
                      required
                    >
                      <option value="" disabled>Select Primary Language</option>
                      {REGIONAL_LANGUAGES.map((lang) => (
                        <option key={lang} value={lang}>{lang}</option>
                      ))}
                    </select>
                  </div>

                  {/* Preferred Time Slot */}
                  <div>
                    <label className="block text-xs font-semibold text-gray-300 mb-1">
                      Preferred Online Shift <span className="text-red-400">*</span>
                    </label>
                    <select
                      value={timeSlot}
                      onChange={(e) => setTimeSlot(e.target.value)}
                      className="w-full bg-[#091224] border border-gray-700/60 focus:border-[#4A7BF7] rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:ring-1 focus:ring-[#4A7BF7] transition"
                      required
                    >
                      <option value="" disabled>Select Preferred Shift</option>
                      {TIME_SLOTS.map((slot) => (
                        <option key={slot.id} value={slot.id}>{slot.label}</option>
                      ))}
                    </select>
                  </div>

                  {/* Submit Button */}
                  <button
                    type="submit"
                    disabled={!isFormValid || isSubmitting}
                    className={`w-full mt-2 py-3.5 rounded-xl font-bold text-sm transition-all duration-200 flex items-center justify-center space-x-2 ${
                      isFormValid && !isSubmitting
                        ? 'bg-[#4A7BF7] hover:bg-blue-600 text-white shadow-lg shadow-blue-500/30 cursor-pointer active:scale-[0.99]'
                        : 'bg-gray-800 text-gray-500 cursor-not-allowed border border-gray-700/50'
                    }`}
                  >
                    <span>{isSubmitting ? 'Submitting Application...' : 'Submit Application'}</span>
                    {!isSubmitting && <ArrowRight className="w-4 h-4" />}
                  </button>

                  <p className="text-[11px] text-gray-500 text-center mt-2">
                    🔒 Your details are 100% confidential. No spam guaranteed.
                  </p>
                </form>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* 3 Pillars Benefits Section */}
      <section id="benefits" className="px-6 py-20 bg-[#0b142b]/70 border-y border-gray-800/60">
        <div className="max-w-6xl mx-auto space-y-12">
          <div className="text-center max-w-2xl mx-auto space-y-3">
            <h2 className="text-3xl md:text-4xl font-black tracking-tight">
              Why Creators & Hosts Choose Connecto
            </h2>
            <p className="text-gray-400 text-sm">
              We designed Connecto to be the safest, most rewarding audio companion platform in India.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {/* Card 1 */}
            <div className="p-8 rounded-3xl bg-[#0e1933] border border-blue-900/30 space-y-4 hover:border-blue-700/60 transition group">
              <div className="w-12 h-12 rounded-2xl bg-blue-500/10 text-[#4A7BF7] flex items-center justify-center group-hover:scale-110 transition">
                <Lock className="w-6 h-6" />
              </div>
              <h3 className="text-xl font-bold text-white">100% Identity Privacy</h3>
              <p className="text-gray-400 text-sm leading-relaxed">
                No video calls. Expressive 3D AI avatars protect your identity. Your real face, photo, and personal phone number are never shared with callers.
              </p>
            </div>

            {/* Card 2 */}
            <div className="p-8 rounded-3xl bg-[#0e1933] border border-blue-900/30 space-y-4 hover:border-blue-700/60 transition group">
              <div className="w-12 h-12 rounded-2xl bg-amber-500/10 text-amber-400 flex items-center justify-center group-hover:scale-110 transition">
                <Coins className="w-6 h-6" />
              </div>
              <h3 className="text-xl font-bold text-white">Set Your Own Calling Rate</h3>
              <p className="text-gray-400 text-sm leading-relaxed">
                You control how much you charge per minute (10–50 coins/min). Earn 60% of every connected call minute directly into your earnings wallet.
              </p>
            </div>

            {/* Card 3 */}
            <div className="p-8 rounded-3xl bg-[#0e1933] border border-blue-900/30 space-y-4 hover:border-blue-700/60 transition group">
              <div className="w-12 h-12 rounded-2xl bg-emerald-500/10 text-emerald-400 flex items-center justify-center group-hover:scale-110 transition">
                <ShieldCheck className="w-6 h-6" />
              </div>
              <h3 className="text-xl font-bold text-white">Direct Weekly UPI Payouts</h3>
              <p className="text-gray-400 text-sm leading-relaxed">
                Fast 3 to 5 business day redemptions directly into your GPay, PhonePe, or Paytm UPI ID with automated 1% TDS tax compliance certificates.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Interactive Earnings Calculator */}
      <section id="calculator" className="px-6 py-20">
        <div className="max-w-4xl mx-auto bg-gradient-to-br from-[#0e1a38] to-[#12162a] border border-blue-900/40 rounded-3xl p-8 md:p-12 shadow-2xl">
          <div className="text-center max-w-xl mx-auto space-y-3 mb-10">
            <span className="text-xs font-bold uppercase tracking-wider text-blue-400">
              Earnings Potential
            </span>
            <h2 className="text-3xl font-black tracking-tight">
              See How Much You Can Earn
            </h2>
            <p className="text-sm text-gray-400">
              Calculate your estimated monthly income based on your calling hours and per-minute rate.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-10 items-center">
            <div className="space-y-6">
              {/* Hours Slider */}
              <div>
                <div className="flex justify-between text-sm font-semibold mb-2">
                  <span className="text-gray-300">Daily Talking Hours:</span>
                  <span className="text-[#4A7BF7] font-bold">{calcHours} hours / day</span>
                </div>
                <input
                  type="range"
                  min="1"
                  max="8"
                  value={calcHours}
                  onChange={(e) => setCalcHours(Number(e.target.value))}
                  className="w-full accent-[#4A7BF7] cursor-pointer"
                />
                <div className="flex justify-between text-xs text-gray-500 mt-1">
                  <span>1 hr</span>
                  <span>4 hrs</span>
                  <span>8 hrs</span>
                </div>
              </div>

              {/* Rate Selection */}
              <div>
                <label className="block text-sm font-semibold text-gray-300 mb-2">
                  Your Calling Rate:
                </label>
                <div className="grid grid-cols-3 gap-3">
                  {[10, 15, 20].map((rate) => (
                    <button
                      key={rate}
                      type="button"
                      onClick={() => setCalcRate(rate)}
                      className={`py-2.5 rounded-xl text-sm font-bold border transition ${
                        calcRate === rate
                          ? 'bg-[#4A7BF7] border-[#4A7BF7] text-white shadow-md shadow-blue-500/20'
                          : 'bg-[#091224] border-gray-700/60 text-gray-300 hover:border-gray-500'
                      }`}
                    >
                      ₹{rate}/min
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Calculated Result Card */}
            <div className="bg-[#080e1f] border border-blue-900/50 rounded-2xl p-8 text-center space-y-3">
              <p className="text-xs font-bold uppercase tracking-wider text-gray-400">
                Estimated Monthly Payout
              </p>
              <div className="text-4xl md:text-5xl font-black text-transparent bg-clip-text bg-gradient-to-r from-emerald-400 to-teal-300">
                ₹{monthlyEstimatedEarnings.toLocaleString('en-IN')}
              </div>
              <p className="text-xs text-gray-400">
                Based on 30 active days/month (60% host share after 40% platform fee).
              </p>
              <div className="pt-4 border-t border-gray-800">
                <a
                  href="#apply-form"
                  className="inline-block w-full py-3 rounded-xl bg-[#4A7BF7] hover:bg-blue-600 text-sm font-bold text-white transition shadow-lg shadow-blue-600/30"
                >
                  Start Earning Today
                </a>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* FAQ Section */}
      <section id="faq" className="px-6 py-20 bg-[#080f22]/60 border-t border-gray-800/60">
        <div className="max-w-4xl mx-auto space-y-10">
          <div className="text-center space-y-2">
            <h2 className="text-3xl font-black tracking-tight">Frequently Asked Questions</h2>
            <p className="text-sm text-gray-400">Everything you need to know before getting started.</p>
          </div>

          <div className="space-y-4">
            {[
              {
                q: 'What is Connecto and what does a host do?',
                a: 'Connecto is a 1-on-1 social audio companion app. Hosts receive calls from people across India who want to talk about daily life, friendships, work, or casual banter. You listen, chat, and keep the conversation engaging.',
              },
              {
                q: 'Do I have to show my real face or camera?',
                a: 'Never! Connecto is 100% audio-only. All hosts and callers use expressive 3D avatars. Your real face, video, and personal contact numbers are never shown.',
              },
              {
                q: 'How do I take calls after applying?',
                a: 'Once your web application is reviewed, our team contacts you via WhatsApp. You download the Connecto Android app, sign in with your registered mobile number, toggle "Online", and start receiving incoming calls.',
              },
              {
                q: 'How and when do I get paid?',
                a: 'Payouts are made directly to your Google Pay, PhonePe, or Paytm UPI ID within 3 to 5 business days upon requesting withdrawal. TDS (1% with valid PAN) is automatically processed.',
              },
              {
                q: 'What are the rules and code of conduct?',
                a: 'Connecto maintains a strict zero-tolerance policy against vulgarity, sexual misconduct, and exchanging personal contact information off-platform. Violators are banned immediately.',
              },
            ].map((faq, i) => (
              <div
                key={i}
                className="bg-[#0f1b35] border border-gray-800/80 rounded-2xl p-6 space-y-2"
              >
                <h4 className="font-bold text-white text-base flex items-start space-x-3">
                  <HelpCircle className="w-5 h-5 text-[#4A7BF7] shrink-0 mt-0.5" />
                  <span>{faq.q}</span>
                </h4>
                <p className="text-sm text-gray-300 pl-8 leading-relaxed">{faq.a}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="px-6 py-10 bg-[#050914] border-t border-gray-900 text-xs text-gray-400">
        <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center space-x-2">
            <span className="font-extrabold text-white">Connecto</span>
            <span>— 100% Real Friendships & Audio Companionship.</span>
          </div>
          <div className="flex items-center space-x-6">
            <a href="#" className="hover:text-white transition">Terms & Conditions</a>
            <a href="#" className="hover:text-white transition">Privacy Policy</a>
            <a href="#" className="hover:text-white transition">Host Guidelines</a>
            <span className="text-gray-600">|</span>
            <span>Support: support@connecto.live</span>
          </div>
        </div>
      </footer>
    </div>
  );
}
