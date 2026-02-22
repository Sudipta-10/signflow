import Link from "next/link";

export default function Home() {
  return (
    <div className="min-h-screen bg-black text-white selection:bg-blue-500/30">
      {/* Navigation */}
      <nav className="flex items-center justify-between px-6 py-4 border-b border-white/10 backdrop-blur-md sticky top-0 z-50">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center font-bold text-lg shadow-[0_0_15px_rgba(59,130,246,0.5)]">
            S
          </div>
          <span className="text-xl font-semibold tracking-tight">Signflow</span>
        </div>
        <div className="flex gap-4">
          <Link href="/login" className="px-5 py-2 text-sm font-medium text-zinc-300 hover:text-white transition-colors">
            Log in
          </Link>
          <Link href="/login" className="px-5 py-2 text-sm font-medium bg-white text-black rounded-full hover:bg-zinc-200 transition-all hover:scale-105 active:scale-95 shadow-[0_0_20px_rgba(255,255,255,0.2)]">
            Get Started
          </Link>
        </div>
      </nav>

      {/* Hero Section */}
      <main className="flex flex-col items-center justify-center px-4 pt-32 pb-24 text-center">
        <div className="inline-flex items-center rounded-full border border-blue-500/30 bg-blue-500/10 px-3 py-1 text-sm font-medium text-blue-400 mb-8 backdrop-blur-sm">
          <span className="flex h-2 w-2 rounded-full bg-blue-500 mr-2 animate-pulse"></span>
          Signflow is now in Beta
        </div>

        <h1 className="max-w-4xl text-5xl md:text-7xl font-extrabold tracking-tight mb-8 bg-gradient-to-b from-white to-zinc-500 bg-clip-text text-transparent">
          The seamless way to <br className="hidden md:block" /> request and sign documents.
        </h1>

        <p className="max-w-2xl text-lg md:text-xl text-zinc-400 mb-12 font-light leading-relaxed">
          Signflow provides a secure, fast, and beautiful experience for executing digital signatures. Keep your workflows moving effortlessly.
        </p>

        <div className="flex flex-col sm:flex-row gap-4 w-full justify-center max-w-md">
          <Link href="/login" className="flex items-center justify-center w-full px-8 py-4 text-base font-medium bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-full hover:opacity-90 transition-all hover:shadow-[0_0_30px_rgba(59,130,246,0.4)] hover:-translate-y-1">
            Start signing for free
          </Link>
          <a href="#features" className="flex items-center justify-center w-full px-8 py-4 text-base font-medium border border-zinc-800 rounded-full hover:bg-zinc-900 transition-colors">
            Learn more
          </a>
        </div>
      </main>

      {/* Features Outline */}
      <section id="features" className="py-24 px-6 border-t border-white/5 bg-zinc-950/50">
        <div className="max-w-6xl mx-auto flex flex-col md:flex-row gap-12">
          {[{
            title: "Legally Binding",
            desc: "Secure e-signatures that comply with global ESIGN and UETA standards.",
            icon: "⚖️"
          }, {
            title: "Lightning Fast",
            desc: "Send requests in seconds with reusable templates and bulk options.",
            icon: "⚡"
          }, {
            title: "Real-time Tracking",
            desc: "Know exactly when your documents are opened, viewed, and signed.",
            icon: "👀"
          }].map((feature, i) => (
            <div key={i} className="flex-1 p-8 rounded-2xl bg-zinc-900/50 border border-white/5 hover:border-blue-500/30 transition-colors group">
              <div className="text-3xl mb-4 p-3 bg-zinc-800 inline-block rounded-xl group-hover:scale-110 transition-transform">
                {feature.icon}
              </div>
              <h3 className="text-xl font-semibold mb-2">{feature.title}</h3>
              <p className="text-zinc-400 leading-relaxed">{feature.desc}</p>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
