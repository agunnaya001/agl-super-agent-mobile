package com.example.data.repository

import com.example.data.model.LearningLesson
import com.example.data.model.QuizOption
import com.example.data.model.QuizQuestion

object LearningLessonsData {

    val lessons: List<LearningLesson> = listOf(
        LearningLesson(
            id = "lesson_blockchain_fundamentals",
            title = "Blockchain Fundamentals",
            category = "Core Basics",
            durationMinutes = 3,
            xpReward = 100,
            iconEmoji = "⛓️",
            shortSummary = "Learn how distributed ledgers, cryptographic hashing, and consensus mechanisms power trustless networks.",
            contentMarkdown = """
                ### What is a Blockchain?
                A blockchain is an open, distributed ledger that records transactions across a network of computers. Once recorded, data inside a block cannot be altered without changing all subsequent blocks.
                
                ### Key Pillars:
                1. **Decentralization**: No single central authority (like a bank or government) controls the ledger.
                2. **Immutability**: Cryptographic hash pointers link blocks in an unbreakable chronological chain.
                3. **Consensus Mechanisms**: Protocols like Proof of Stake (PoS) validate transactions and prevent double-spending.
                
                ### Why It Matters:
                Blockchains enable digital scarcity, verifiable provenance, and peer-to-peer economic activity without intermediaries.
            """.trimIndent(),
            keyTakeaways = listOf(
                "Blockchains are decentralized, immutable ledgers.",
                "Consensus mechanisms validate transactions without central intermediaries.",
                "Cryptographic hashing ensures tamper-evident block linkages."
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_bf_1",
                    question = "What makes data recorded on a blockchain immutable?",
                    options = listOf(
                        QuizOption(1, "A single administrator locks the database with a password"),
                        QuizOption(2, "Cryptographic hashes link each block to the previous one across a decentralized network"),
                        QuizOption(3, "Transactions are kept completely secret and private"),
                        QuizOption(4, "Data is erased every 24 hours")
                    ),
                    correctOptionId = 2,
                    explanation = "Cryptographic hashing links every block to the previous block's hash. Modifying any past block would invalidate all subsequent hashes across the distributed network."
                )
            )
        ),
        LearningLesson(
            id = "lesson_ethereum",
            title = "Ethereum & EVM",
            category = "Layer 1",
            durationMinutes = 4,
            xpReward = 120,
            iconEmoji = "🔷",
            shortSummary = "Discover the world computer, Ethereum Virtual Machine (EVM), and programmable smart contracts.",
            contentMarkdown = """
                ### The World Computer
                Unlike Bitcoin's scripting language designed primarily for value transfer, Ethereum introduced a Turing-complete state machine: the Ethereum Virtual Machine (EVM).
                
                ### How the EVM Works:
                - **State Transition**: Every block updates the global state of accounts, balances, and contract memory.
                - **Gas**: Every computational operation costs 'gas' in ETH to prevent infinite loops and denial-of-service attacks.
                - **Smart Contracts**: Self-executing code stored at a deterministic address on-chain.
            """.trimIndent(),
            keyTakeaways = listOf(
                "EVM is a decentralized computing environment.",
                "Gas compensates network validators and prevents infinite computational loops.",
                "Smart contracts execute autonomously when conditions are met."
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_eth_1",
                    question = "Why does the EVM require users to pay 'gas' for transactions?",
                    options = listOf(
                        QuizOption(1, "To purchase physical hardware for miners"),
                        QuizOption(2, "To compensate validators and prevent computational denial-of-service spam"),
                        QuizOption(3, "To convert ETH into US Dollars automatically"),
                        QuizOption(4, "Gas is only needed when transactions fail")
                    ),
                    correctOptionId = 2,
                    explanation = "Gas limits computation time and compensates validators for resources, ensuring bad actors cannot spam or freeze the network with infinite loops."
                )
            )
        ),
        LearningLesson(
            id = "lesson_base_l2",
            title = "Base & Layer 2 Rollups",
            category = "Layer 2",
            durationMinutes = 3,
            xpReward = 150,
            iconEmoji = "🔵",
            shortSummary = "Understand how Base utilizes Optimistic Rollups to scale Ethereum with ultra-low fees and sub-second speeds.",
            contentMarkdown = """
                ### What is Base?
                Base is a secure, low-cost, developer-friendly Ethereum Layer 2 (L2) built on the open-source OP Stack, incubated by Coinbase.
                
                ### How Optimistic Rollups Work:
                - **Off-chain Execution**: Transactions are processed off Ethereum Mainnet (L1) at lightning speeds.
                - **Batching & Compression**: Thousands of transactions are bundled and posted to L1 calldata/blobs (EIP-4844).
                - **Optimism**: Assumes state transitions are valid unless challenged via fraud proofs within a challenge period.
                
                ### Benefits on Base:
                Transactions cost fractions of a cent while inheriting the complete cryptographic security of Ethereum L1.
            """.trimIndent(),
            keyTakeaways = listOf(
                "Base is an Optimistic Rollup L2 built on the OP Stack.",
                "Bundles thousands of transactions together for ultra-low fees.",
                "Inherits Ethereum L1 security without sacrificing speed."
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_base_1",
                    question = "How does Base achieve lower gas fees compared to Ethereum L1?",
                    options = listOf(
                        QuizOption(1, "By executing transactions off-chain and batching compressed proofs to L1"),
                        QuizOption(2, "By removing cryptographic security entirely"),
                        QuizOption(3, "By only allowing single-user transactions"),
                        QuizOption(4, "By running on centralized bank servers")
                    ),
                    correctOptionId = 1,
                    explanation = "Base processes execution off-chain and batches hundreds of transactions into compressed L1 data blobs, dividing the L1 security cost across many users."
                )
            )
        ),
        LearningLesson(
            id = "lesson_wallets",
            title = "Web3 Wallets & Self-Custody",
            category = "Security",
            durationMinutes = 4,
            xpReward = 120,
            iconEmoji = "👛",
            shortSummary = "Public keys, private keys, seed phrases, and how to safeguard your self-custody identity.",
            contentMarkdown = """
                ### Public Key vs. Private Key
                - **Public Key (Address)**: Like your email address or bank account number. Safe to share with anyone to receive funds.
                - **Private Key / Seed Phrase**: The master cryptographic secret that signs transactions and authorizes transfers. Never share it with anyone!
                
                ### Golden Custody Rules:
                1. Never input your seed phrase into websites or send it via chat/support.
                2. AGL Super Agent operates in read-only and watch mode: it **never** requests your private key.
                3. Use hardware wallets or smart contract wallets (ERC-4337 Account Abstraction) for large portfolios.
            """.trimIndent(),
            keyTakeaways = listOf(
                "Your public address is for receiving funds; your private key authorizes spending.",
                "Never share your 12/24-word seed phrase under any circumstances.",
                "Legitimate applications never ask for private keys."
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_w_1",
                    question = "Which credential should NEVER be shared with anyone or entered into an app?",
                    options = listOf(
                        QuizOption(1, "Public wallet address (0x...)"),
                        QuizOption(2, "Transaction hash"),
                        QuizOption(3, "Private key or 12/24-word seed phrase"),
                        QuizOption(4, "Token contract address")
                    ),
                    correctOptionId = 3,
                    explanation = "Your private key or seed phrase grants complete control over your assets. Legitimate Web3 apps and support agents will never ask for it."
                )
            )
        ),
        LearningLesson(
            id = "lesson_tokens",
            title = "Tokens: ERC-20 & Standards",
            category = "Assets",
            durationMinutes = 3,
            xpReward = 100,
            iconEmoji = "🪙",
            shortSummary = "Learn how fungible tokens work, what allowances mean, and how decimals operate.",
            contentMarkdown = """
                ### Fungibility & The ERC-20 Standard
                A fungible token is interchangeable: 1 AGL is always equivalent in value to any other 1 AGL.
                
                ### Core Functions in ERC-20:
                - `balanceOf(address)`: Returns token balance.
                - `transfer(to, amount)`: Moves tokens from caller to recipient.
                - `approve(spender, amount)`: Authorizes a smart contract (e.g. DEX or Staking Vault) to spend tokens on your behalf.
            """.trimIndent(),
            keyTakeaways = listOf(
                "ERC-20 defines the universal interface for fungible tokens on EVM networks.",
                "`approve()` allows external contracts to interact with your tokens.",
                "Always check approval limits before confirming."
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_tok_1",
                    question = "What does the `approve()` method do in an ERC-20 token contract?",
                    options = listOf(
                        QuizOption(1, "It burns all tokens immediately"),
                        QuizOption(2, "It authorizes a smart contract or spender to transfer a specified token allowance"),
                        QuizOption(3, "It changes your wallet password"),
                        QuizOption(4, "It cancels past transactions")
                    ),
                    correctOptionId = 2,
                    explanation = "`approve` allows a designated smart contract (like Uniswap or AGL Staking Vault) to pull a specific amount of tokens from your wallet when executing an action."
                )
            )
        ),
        LearningLesson(
            id = "lesson_nfts",
            title = "NFTs & Digital Ownership",
            category = "Assets",
            durationMinutes = 3,
            xpReward = 100,
            iconEmoji = "🖼️",
            shortSummary = "Explore ERC-721 and ERC-1155 non-fungible tokens, digital scarcity, and metadata standards.",
            contentMarkdown = """
                ### What makes an NFT unique?
                Unlike ERC-20 tokens, non-fungible tokens (ERC-721 / ERC-1155) have unique `tokenURI` identifiers and metadata pointing to decentralized storage (IPFS/Arweave).
                
                ### Web3 Use Cases:
                - Digital identity and Super Agent badges
                - Decentralized domain names (.base, .eth)
                - Gaming items and metaverse assets
                - Real-world asset (RWA) tokenization
            """.trimIndent(),
            keyTakeaways = listOf(
                "NFTs represent unique, non-interchangeable digital assets.",
                "ERC-721 is the single-item standard; ERC-1155 is the multi-token standard.",
                "Metadata is linked via immutable hashes or decentralized storage."
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_nft_1",
                    question = "What distinguishes an ERC-721 token from an ERC-20 token?",
                    options = listOf(
                        QuizOption(1, "ERC-721 tokens are unique and non-interchangeable with distinct IDs"),
                        QuizOption(2, "ERC-721 tokens do not use cryptography"),
                        QuizOption(3, "ERC-721 tokens can only exist on Bitcoin"),
                        QuizOption(4, "There is no difference")
                    ),
                    correctOptionId = 1,
                    explanation = "ERC-721 defines non-fungible tokens where each token possesses a unique identifier and individual metadata."
                )
            )
        ),
        LearningLesson(
            id = "lesson_smart_contracts",
            title = "Smart Contract Architecture",
            category = "Architecture",
            durationMinutes = 4,
            xpReward = 140,
            iconEmoji = "🧠",
            shortSummary = "Bytecode, ABIs, proxy upgrade patterns, and reentrancy protections explained simply.",
            contentMarkdown = """
                ### What is a Smart Contract?
                A smart contract is deterministic program code compiled to bytecode and deployed on the blockchain.
                
                ### Key Concepts:
                - **ABI (Application Binary Interface)**: The translator dictionary that allows apps like AGL Super Agent to encode and decode function calls and events.
                - **Proxy Contracts**: A pattern where a proxy delegates execution calls to an implementation logic contract, allowing upgradeability.
                - **Admin Controls & Timelocks**: Multi-sig wallets or timelock delays that govern critical parameter changes.
            """.trimIndent(),
            keyTakeaways = listOf(
                "ABIs specify how external apps interact with contract bytecode.",
                "Proxy patterns separate state storage from upgradeable logic.",
                "Timelocks give communities time to review contract parameter upgrades."
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_sc_1",
                    question = "What is the purpose of an ABI (Application Binary Interface)?",
                    options = listOf(
                        QuizOption(1, "It prints paper receipts for transactions"),
                        QuizOption(2, "It defines the interface enabling applications to encode function calls and decode on-chain events"),
                        QuizOption(3, "It acts as a physical security key"),
                        QuizOption(4, "It replaces the need for blockchain consensus")
                    ),
                    correctOptionId = 2,
                    explanation = "The ABI tells client applications and wallets what functions exist, their input parameters, and how to encode/decode data sent to the smart contract."
                )
            )
        ),
        LearningLesson(
            id = "lesson_defi",
            title = "DeFi & Automated Market Makers",
            category = "DeFi",
            durationMinutes = 4,
            xpReward = 140,
            iconEmoji = "📈",
            shortSummary = "Automated Market Makers (AMMs), liquidity pools, yield farming, and slippage tolerance.",
            contentMarkdown = """
                ### Decentralized Finance (DeFi)
                DeFi replaces centralized financial intermediaries with autonomous liquidity pools and math formulas.
                
                ### AMM Constant Product Formula:
                `x * y = k`
                - Liquidity providers deposit token pairs into a pool.
                - Traders swap against the pool, paying a small liquidity provider fee.
                - **Slippage**: The difference between expected price and executed price due to pool depth and trade volume.
            """.trimIndent(),
            keyTakeaways = listOf(
                "AMMs use algorithmic liquidity pools rather than traditional order books.",
                "Liquidity providers earn fees from swaps in the pool.",
                "Slippage protection protects users from front-running and high price impacts."
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_defi_1",
                    question = "What determines token prices in an Automated Market Maker (AMM) liquidity pool?",
                    options = listOf(
                        QuizOption(1, "A central government agency sets the rate daily"),
                        QuizOption(2, "The ratio of token reserves in the pool maintained by math formulas like x * y = k"),
                        QuizOption(3, "Random number generators"),
                        QuizOption(4, "The physical location of the server")
                    ),
                    correctOptionId = 2,
                    explanation = "AMMs calculate token prices algorithmically based on the proportional ratio of the two assets deposited in the liquidity pool."
                )
            )
        ),
        LearningLesson(
            id = "lesson_daos",
            title = "DAOs & Decentralized Governance",
            category = "Governance",
            durationMinutes = 3,
            xpReward = 110,
            iconEmoji = "🏛️",
            shortSummary = "How token voting, quorum thresholds, and on-chain proposal execution shape decentralized protocols.",
            contentMarkdown = """
                ### Decentralized Autonomous Organizations (DAOs)
                A DAO is a community-led entity with no central leadership, governed by on-chain voting rules encoded in smart contracts.
                
                ### Governance Lifecycle:
                1. **Discussion & RFC**: Community drafts idea on forum.
                2. **Proposal Submission**: Proposer stakes tokens to open on-chain vote.
                3. **Quorum & Voting**: Token holders cast cryptographic votes proportional to voting power.
                4. **Timelock Execution**: Approved proposals execute automatically after security delay.
            """.trimIndent(),
            keyTakeaways = listOf(
                "DAOs organize collective decision-making via token-weighted votes.",
                "Proposals require minimum quorum thresholds to pass.",
                "Timelocks provide a safety buffer before code changes take effect."
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_dao_1",
                    question = "What role does a Timelock contract play in DAO governance?",
                    options = listOf(
                        QuizOption(1, "It permanently freezes all user funds"),
                        QuizOption(2, "It enforces a time delay between proposal approval and execution for security review"),
                        QuizOption(3, "It speeds up transactions to zero seconds"),
                        QuizOption(4, "It is used for mining new tokens")
                    ),
                    correctOptionId = 2,
                    explanation = "Timelocks introduce a mandatory security delay (e.g. 24-48 hours) so users have time to inspect approved actions before they are executed on-chain."
                )
            )
        ),
        LearningLesson(
            id = "lesson_gamefi",
            title = "GameFi & On-Chain Rewards",
            category = "Gaming",
            durationMinutes = 3,
            xpReward = 120,
            iconEmoji = "🎮",
            shortSummary = "Play-and-earn mechanics, quest progression, verifiable randomness (VRF), and token economies.",
            contentMarkdown = """
                ### What is GameFi?
                GameFi combines video game mechanics with decentralized finance incentives, allowing players to own their in-game items, earn XP, and unlock real crypto rewards.
                
                ### AGL Super Agent GameFi Features:
                - **Daily & Weekly Missions**: Complete security audits and learning modules to level up.
                - **Proof of Action**: On-chain verification of mission completion.
                - **Dynamic Super Agent Tiers**: Higher tiers earn boosted staking APR and governance weight.
            """.trimIndent(),
            keyTakeaways = listOf(
                "GameFi bridges gaming achievements with verifiable asset ownership.",
                "XP progression unlocks tiered privileges and ecosystem rewards.",
                "Missions encourage security hygiene and continuous ecosystem exploration."
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_gf_1",
                    question = "What is the primary benefit of on-chain GameFi assets?",
                    options = listOf(
                        QuizOption(1, "True player ownership and interoperability across open ecosystems"),
                        QuizOption(2, "Games become impossible to play"),
                        QuizOption(3, "All graphics are stored as raw text on the monitor"),
                        QuizOption(4, "Items expire automatically after one game")
                    ),
                    correctOptionId = 1,
                    explanation = "GameFi ensures digital assets belong to the player's wallet, allowing trading, staking, or using them across multiple compatible dApps."
                )
            )
        ),
        LearningLesson(
            id = "lesson_web3_security",
            title = "Web3 Security & Threat Vectors",
            category = "Security",
            durationMinutes = 5,
            xpReward = 200,
            iconEmoji = "🛡️",
            shortSummary = "Master defenses against phishing, drainers, honeypots, reentrancy attacks, and malicious approvals.",
            contentMarkdown = """
                ### Common Web3 Security Threats:
                1. **Phishing & Drainers**: Malicious websites that trick users into signing `Permit` or `setApprovalForAll` signatures that grant drainers access to wallet assets.
                2. **Honeypot Tokens**: Fake tokens with modified sell fees (e.g. 99% tax) or disabled transfer functions.
                3. **Fake Airdrops**: Unsolicited tokens prompting users to visit a scam URL to 'claim rewards'.
                
                ### AGL Security Engine Safeguards:
                - Automated risk tier indicators: **LOW CONCERN**, **REVIEW**, **HIGH CONCERN**.
                - Permission audit: Highlights infinite token allowances before you sign.
            """.trimIndent(),
            keyTakeaways = listOf(
                "Always inspect transaction parameters and approval spenders before confirming.",
                "Honeypots allow buying but block or heavily tax selling.",
                "Use AGL AI Security Scanner to audit contracts before interaction."
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_sec_1",
                    question = "What is a Honeypot smart contract scam in crypto?",
                    options = listOf(
                        QuizOption(1, "A legitimate contract giving free honey to users"),
                        QuizOption(2, "A malicious token that allows buying but prevents or heavily taxes selling"),
                        QuizOption(3, "A hardware wallet manufacturer"),
                        QuizOption(4, "An official Base blockchain update")
                    ),
                    correctOptionId = 2,
                    explanation = "A Honeypot token is coded with hidden restrictions that let victims buy tokens but restrict or block selling, trapping user funds inside the pool."
                )
            )
        ),
        LearningLesson(
            id = "lesson_onchain_txs",
            title = "On-Chain Transactions & Gas",
            category = "Transactions",
            durationMinutes = 4,
            xpReward = 130,
            iconEmoji = "⚡",
            shortSummary = "Nonce management, Base gas components (Execution + L1 Data), and transaction states.",
            contentMarkdown = """
                ### Anatomy of a Base Transaction:
                - **Nonce**: Sequence counter ensuring transactions are executed strictly in order.
                - **Gas Limit**: The maximum amount of computational units you authorize for execution.
                - **Base Fee + Priority Tip**: The dynamic cost per gas unit.
                - **L1 Data Fee**: The small fraction paid to submit compressed transaction data to Ethereum L1.
                
                ### Transaction Lifecycle:
                `Pending -> Included in Block -> Sequencer Finality -> L1 State Root Batch Finality`
            """.trimIndent(),
            keyTakeaways = listOf(
                "Nonce ensures transactions execute in exact sequence without duplication.",
                "Base gas fees combine L2 execution cost with low L1 data blob costs.",
                "Sub-second block times provide instantaneous user confirmations on Base."
            ),
            quiz = listOf(
                QuizQuestion(
                    id = "q_tx_1",
                    question = "What does the transaction 'nonce' prevent in blockchain transactions?",
                    options = listOf(
                        QuizOption(1, "It prevents transaction replay attacks and out-of-order execution"),
                        QuizOption(2, "It stops computer viruses from turning on the screen"),
                        QuizOption(3, "It prevents you from reading your balance"),
                        QuizOption(4, "It increases your internet bandwidth")
                    ),
                    correctOptionId = 1,
                    explanation = "The nonce is a monotonically increasing counter for each account that prevents transactions from being replayed multiple times or executing out of order."
                )
            )
        )
    )

    val defaultQuests = listOf(
        com.example.data.model.QuestItem(
            id = "quest_daily_checkin",
            title = "Super Agent Daily Check-In",
            description = "Open the AGL Command Center and sync your Base on-chain telemetry.",
            xpReward = 50,
            aglReward = 2.5,
            category = com.example.data.model.QuestCategory.DAILY,
            currentProgress = 1,
            maxProgress = 1,
            isClaimed = false,
            iconEmoji = "⚡"
        ),
        com.example.data.model.QuestItem(
            id = "quest_ai_explain",
            title = "Ask AI to Explain a Transaction",
            description = "Use the AI Web3 Assistant to decode complex on-chain transaction data into simple English.",
            xpReward = 100,
            aglReward = 5.0,
            category = com.example.data.model.QuestCategory.DAILY,
            currentProgress = 0,
            maxProgress = 1,
            isClaimed = false,
            iconEmoji = "🤖"
        ),
        com.example.data.model.QuestItem(
            id = "quest_scan_contract",
            title = "Scan a Base Smart Contract",
            description = "Run a security and architecture analysis on any Base contract address.",
            xpReward = 150,
            aglReward = 10.0,
            category = com.example.data.model.QuestCategory.WEEKLY,
            currentProgress = 1,
            maxProgress = 1,
            isClaimed = false,
            iconEmoji = "🧠"
        ),
        com.example.data.model.QuestItem(
            id = "quest_learn_lessons",
            title = "Complete 3 Learning Quizzes",
            description = "Advance your Web3 knowledge in the Learning Center by acing 3 educational quizzes.",
            xpReward = 300,
            aglReward = 20.0,
            category = com.example.data.model.QuestCategory.WEEKLY,
            currentProgress = 1,
            maxProgress = 3,
            isClaimed = false,
            iconEmoji = "📚"
        ),
        com.example.data.model.QuestItem(
            id = "quest_stake_agl",
            title = "Stake AGL in AI Compute Vault",
            description = "Commit AGL tokens into the decentralized staking vault to secure agent nodes.",
            xpReward = 500,
            aglReward = 35.0,
            category = com.example.data.model.QuestCategory.ECOSYSTEM,
            currentProgress = 1,
            maxProgress = 1,
            isClaimed = true,
            iconEmoji = "🔒"
        ),
        com.example.data.model.QuestItem(
            id = "quest_security_audit",
            title = "Perform Full Security Risk Audit",
            description = "Audit an address, transaction hash, or token contract using the Web3 Security Assistant.",
            xpReward = 250,
            aglReward = 15.0,
            category = com.example.data.model.QuestCategory.SECURITY_CHALLENGE,
            currentProgress = 0,
            maxProgress = 1,
            isClaimed = false,
            iconEmoji = "🛡️"
        )
    )

    val defaultBadges = listOf(
        com.example.data.model.AchievementBadge(
            id = "badge_genesis",
            title = "Genesis Agent",
            description = "Joined the AGL Super Agent network on Base.",
            iconEmoji = "🚀",
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis() - 86400000L * 7
        ),
        com.example.data.model.AchievementBadge(
            id = "badge_security_guardian",
            title = "Security Sentinel",
            description = "Completed smart contract scans and audited risky permissions.",
            iconEmoji = "🛡️",
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis() - 86400000L * 3
        ),
        com.example.data.model.AchievementBadge(
            id = "badge_scholar",
            title = "Web3 Scholar",
            description = "Completed 5 lessons in the Web3 Learning Center.",
            iconEmoji = "🎓",
            isUnlocked = false
        ),
        com.example.data.model.AchievementBadge(
            id = "badge_staking_master",
            title = "Vault Archon",
            description = "Staked over 250 AGL tokens into the AI Compute Vault.",
            iconEmoji = "💎",
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis() - 86400000L * 2
        ),
        com.example.data.model.AchievementBadge(
            id = "badge_ai_whisperer",
            title = "AI Strategist",
            description = "Queried the AI Web3 Assistant for deep blockchain analytics 10+ times.",
            iconEmoji = "🧠",
            isUnlocked = false
        ),
        com.example.data.model.AchievementBadge(
            id = "badge_base_explorer",
            title = "Base Pioneer",
            description = "Explored 5+ verified Base L2 contracts and transaction hashes.",
            iconEmoji = "🌐",
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis() - 86400000L * 4
        )
    )

    fun getLeaderboard(timeframe: com.example.data.model.LeaderboardTimeframe): List<com.example.data.model.LeaderboardUser> {
        val multiplier = when (timeframe) {
            com.example.data.model.LeaderboardTimeframe.DAILY -> 1.0
            com.example.data.model.LeaderboardTimeframe.WEEKLY -> 4.5
            com.example.data.model.LeaderboardTimeframe.MONTHLY -> 18.0
            com.example.data.model.LeaderboardTimeframe.ALL_TIME -> 42.0
        }

        return listOf(
            com.example.data.model.LeaderboardUser(
                rank = 1,
                address = "0x981A...73f1",
                username = "BaseArchon.eth",
                xp = (18500 * multiplier).toLong(),
                level = 38,
                tier = com.example.data.model.SuperAgentTier.DIAMOND,
                questsCompleted = (94 * multiplier / 4).toInt(),
                avatarEmoji = "👑"
            ),
            com.example.data.model.LeaderboardUser(
                rank = 2,
                address = "0x44B2...290C",
                username = "SatoshiBase",
                xp = (16200 * multiplier).toLong(),
                level = 34,
                tier = com.example.data.model.SuperAgentTier.DIAMOND,
                questsCompleted = (82 * multiplier / 4).toInt(),
                avatarEmoji = "⚡"
            ),
            com.example.data.model.LeaderboardUser(
                rank = 3,
                address = "0x89C1...F091",
                username = "CyberAuditor",
                xp = (14100 * multiplier).toLong(),
                level = 29,
                tier = com.example.data.model.SuperAgentTier.GOLD,
                questsCompleted = (71 * multiplier / 4).toInt(),
                avatarEmoji = "🛡️"
            ),
            com.example.data.model.LeaderboardUser(
                rank = 4,
                address = "0x742d...f44e",
                username = "You (Super Agent)",
                xp = (12450 * multiplier).toLong(),
                level = 24,
                tier = com.example.data.model.SuperAgentTier.GOLD,
                questsCompleted = (58 * multiplier / 4).toInt(),
                isCurrentUser = true,
                avatarEmoji = "🤖"
            ),
            com.example.data.model.LeaderboardUser(
                rank = 5,
                address = "0x22D3...90AA",
                username = "DegenScholar",
                xp = (10800 * multiplier).toLong(),
                level = 21,
                tier = com.example.data.model.SuperAgentTier.SILVER,
                questsCompleted = (49 * multiplier / 4).toInt(),
                avatarEmoji = "🎩"
            ),
            com.example.data.model.LeaderboardUser(
                rank = 6,
                address = "0x55E9...11CB",
                username = "AeroPilot",
                xp = (9200 * multiplier).toLong(),
                level = 18,
                tier = com.example.data.model.SuperAgentTier.SILVER,
                questsCompleted = (42 * multiplier / 4).toInt(),
                avatarEmoji = "✈️"
            ),
            com.example.data.model.LeaderboardUser(
                rank = 7,
                address = "0x11FF...6543",
                username = "BaseNovice",
                xp = (6400 * multiplier).toLong(),
                level = 12,
                tier = com.example.data.model.SuperAgentTier.BRONZE,
                questsCompleted = (28 * multiplier / 4).toInt(),
                avatarEmoji = "🌱"
            )
        )
    }
}
